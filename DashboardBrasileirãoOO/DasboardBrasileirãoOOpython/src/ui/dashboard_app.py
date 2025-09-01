import sys
import pandas as pd
from PyQt6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QPushButton,
    QComboBox, QCheckBox, QLabel, QTableWidget, QTableWidgetItem,
    QTabWidget, QAbstractScrollArea, QHeaderView, QMessageBox, QSizePolicy,
    QScrollArea
)
from PyQt6.QtCore import Qt
from analise.analisador_campeonato import AnalisadorCampeonato
from dados.leitor_csv import LeitorCSV
from modelo.time import Time
from modelo.partida import Partida
from typing import List

import matplotlib.pyplot as plt
from matplotlib.backends.backend_qt5agg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure
import numpy as np
import re

class MplCanvas(FigureCanvas):
    def __init__(self, parent=None, width=5, height=4, dpi=100):
        fig = Figure(figsize=(width, height), dpi=dpi)
        self.axes = fig.add_subplot(111)
        super().__init__(fig)
        self.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding)

class DashboardApp(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Dashboard Brasileirão POO")
        self.setGeometry(100, 100, 1200, 800)

        self.analisador: AnalisadorCampeonato = None
        self.todas_as_partidas_do_periodo: pd.DataFrame = pd.DataFrame()
        self.ui_initialized = False
        
        self.central_widget = QWidget()
        self.setCentralWidget(self.central_widget)
        self.main_layout = QVBoxLayout(self.central_widget)
        
        self.painel_superior = self._criar_painel_superior()
        self.main_layout.addWidget(self.painel_superior)
        
        self.tab_widget = QTabWidget()
        self.main_layout.addWidget(self.tab_widget)
        
        self._criar_aba_graficos()
        self._criar_aba_classificacao()
        self._criar_aba_analise_time()
        
        self.ui_initialized = True
        self._carregar_dados_do_periodo()
    
    def _criar_painel_superior(self):
        painel_superior = QWidget()
        layout_superior = QVBoxLayout(painel_superior)

        layout_anos = QHBoxLayout()
        self.seletor_ano_inicio = QComboBox()
        self.seletor_ano_fim = QComboBox()
        for ano in range(2015, 2024):
            self.seletor_ano_inicio.addItem(str(ano))
            self.seletor_ano_fim.addItem(str(ano))
        self.seletor_ano_inicio.setCurrentText("2023")
        self.seletor_ano_fim.setCurrentText("2023")
        
        btn_analisar = QPushButton("Analisar")
        btn_analisar.clicked.connect(self._carregar_dados_do_periodo)

        layout_anos.addWidget(QLabel("De:"))
        layout_anos.addWidget(self.seletor_ano_inicio)
        layout_anos.addWidget(QLabel("Até:"))
        layout_anos.addWidget(self.seletor_ano_fim)
        layout_anos.addWidget(btn_analisar)

        layout_filtros = QHBoxLayout()
        self.cb_sudeste = QCheckBox("Sudeste")
        self.cb_sul = QCheckBox("Sul")
        self.cb_nordeste = QCheckBox("Nordeste")
        self.cb_centro_oeste = QCheckBox("Centro-Oeste")

        self.cb_sudeste.setChecked(True)
        self.cb_sul.setChecked(True)
        self.cb_nordeste.setChecked(True)
        self.cb_centro_oeste.setChecked(True)
        
        layout_filtros.addWidget(QLabel("Filtrar por Região:"))
        layout_filtros.addWidget(self.cb_sudeste)
        layout_filtros.addWidget(self.cb_sul)
        layout_filtros.addWidget(self.cb_nordeste)
        layout_filtros.addWidget(self.cb_centro_oeste)
        
        self.cb_sudeste.stateChanged.connect(self._filtrar_e_atualizar_analise)
        self.cb_sul.stateChanged.connect(self._filtrar_e_atualizar_analise)
        self.cb_nordeste.stateChanged.connect(self._filtrar_e_atualizar_analise)
        self.cb_centro_oeste.stateChanged.connect(self._filtrar_e_atualizar_analise)
        
        layout_superior.addLayout(layout_anos)
        layout_superior.addLayout(layout_filtros)
        
        return painel_superior

    def _carregar_dados_do_periodo(self):
        ano_inicio = int(self.seletor_ano_inicio.currentText())
        ano_fim = int(self.seletor_ano_fim.currentText())

        if ano_fim < ano_inicio:
            QMessageBox.warning(self, "Erro", "O ano final não pode ser menor que o ano inicial.")
            return

        self.todas_as_partidas_do_periodo = pd.DataFrame()
        for ano in range(ano_inicio, ano_fim + 1):
            nome_arquivo = f"brasileirao_{ano}_completo.csv"
            partidas_do_ano_df = LeitorCSV.carregar_partidas(nome_arquivo)
            if not partidas_do_ano_df.empty:
                self.todas_as_partidas_do_periodo = pd.concat([self.todas_as_partidas_do_periodo, partidas_do_ano_df])
            else:
                QMessageBox.warning(self, "Aviso", f"Não foi possível carregar os dados para o ano {ano}.")

        if self.todas_as_partidas_do_periodo.empty:
            QMessageBox.critical(self, "Erro", "Nenhum dado encontrado para o período selecionado.")
            self._limpar_ui()
            return
        self._filtrar_e_atualizar_analise()

    def _filtrar_e_atualizar_analise(self):
        if self.todas_as_partidas_do_periodo.empty:
            self._limpar_ui()
            self.analisador = None
            return

        regioes_selecionadas = []
        if self.cb_sudeste.isChecked(): regioes_selecionadas.append("Sudeste")
        if self.cb_sul.isChecked(): regioes_selecionadas.append("Sul")
        if self.cb_nordeste.isChecked(): regioes_selecionadas.append("Nordeste")
        if self.cb_centro_oeste.isChecked(): regioes_selecionadas.append("Centro-Oeste")
        
        partidas_filtradas_df = AnalisadorCampeonato.filtrar_por_regioes(self.todas_as_partidas_do_periodo, regioes_selecionadas)
        
        if partidas_filtradas_df.empty:
            self._limpar_ui()
            self.analisador = None
            return
        
        self.analisador = AnalisadorCampeonato(partidas_filtradas_df)
        self._atualizar_tabela_classificacao(regioes_selecionadas)
        self._atualizar_seletor_times(regioes_selecionadas)
        if self.ui_initialized:
            self._atualizar_grafico(self.seletor_tipo_grafico.currentIndex())
        
    def _criar_aba_classificacao(self):
        aba = QWidget()
        layout = QVBoxLayout(aba)
        
        self.titulo_classificacao = QLabel("Tabela de Classificação")
        self.titulo_classificacao.setStyleSheet("font-size: 20px;")
        
        self.checkbox_ordem = QCheckBox("Ver em ordem crescente")
        self.checkbox_ordem.stateChanged.connect(lambda: self._atualizar_tabela_classificacao(self._get_regioes_selecionadas()))
        
        self.tabela_classificacao = QTableWidget()
        self._configurar_tabela_classificacao()
        
        layout.addWidget(self.titulo_classificacao)
        layout.addWidget(self.checkbox_ordem)
        layout.addWidget(self.tabela_classificacao)
        self.tab_widget.addTab(aba, "Classificação Geral")

    def _configurar_tabela_classificacao(self):
        self.tabela_classificacao.setColumnCount(9)
        self.tabela_classificacao.setHorizontalHeaderLabels(['Time', 'P', 'J', 'V', 'E', 'D', 'GM', 'GS', 'SG'])
        self.tabela_classificacao.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.tabela_classificacao.setSizeAdjustPolicy(QAbstractScrollArea.SizeAdjustPolicy.AdjustToContents)

    def _atualizar_tabela_classificacao(self, regioes_selecionadas: List[str]):
        if self.analisador is None:
            self.tabela_classificacao.setRowCount(0)
            return

        dados = self.analisador.get_tabela_classificacao(self.checkbox_ordem.isChecked(), regioes_selecionadas)
        self.tabela_classificacao.setRowCount(len(dados))
        
        for i, time in enumerate(dados):
            self.tabela_classificacao.setItem(i, 0, QTableWidgetItem(time.nome))
            self.tabela_classificacao.setItem(i, 1, QTableWidgetItem(str(time.pontos)))
            self.tabela_classificacao.setItem(i, 2, QTableWidgetItem(str(time.jogos)))
            self.tabela_classificacao.setItem(i, 3, QTableWidgetItem(str(time.vitorias)))
            self.tabela_classificacao.setItem(i, 4, QTableWidgetItem(str(time.empates)))
            self.tabela_classificacao.setItem(i, 5, QTableWidgetItem(str(time.derrotas)))
            self.tabela_classificacao.setItem(i, 6, QTableWidgetItem(str(time.gols_pro)))
            self.tabela_classificacao.setItem(i, 7, QTableWidgetItem(str(time.gols_contra)))
            self.tabela_classificacao.setItem(i, 8, QTableWidgetItem(str(time.saldo_gols)))
            
    def _criar_aba_analise_time(self):
        aba = QWidget()
        layout = QVBoxLayout(aba)
        
        titulo = QLabel("Selecione um time para ver seus jogos:")
        titulo.setStyleSheet("font-size: 16px;")

        self.seletor_times_aba_analise = QComboBox()
        self.tabela_jogos_time = QTableWidget()
        self._configurar_tabela_partidas(self.tabela_jogos_time)

        self.seletor_times_aba_analise.currentIndexChanged.connect(self._atualizar_tabela_jogos_time)
        
        layout.addWidget(titulo)
        layout.addWidget(self.seletor_times_aba_analise)
        layout.addWidget(self.tabela_jogos_time)
        self.tab_widget.addTab(aba, "Análise por Time")

    def _configurar_tabela_partidas(self, tabela):
        tabela.setColumnCount(4)
        tabela.setHorizontalHeaderLabels(['Rodada', 'Time Casa', 'Placar', 'Time Fora'])
        tabela.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)

    def _atualizar_tabela_jogos_time(self, index):
        if self.analisador is None or index == -1:
            self.tabela_jogos_time.setRowCount(0)
            return
            
        time_selecionado = self.seletor_times_aba_analise.currentText()
        jogos_do_time = self.analisador.get_partidas_por_time(time_selecionado)
        
        self.tabela_jogos_time.setRowCount(len(jogos_do_time))
        for i, partida in enumerate(jogos_do_time):
            self.tabela_jogos_time.setItem(i, 0, QTableWidgetItem(str(partida.rodada)))
            self.tabela_jogos_time.setItem(i, 1, QTableWidgetItem(partida.time_casa))
            self.tabela_jogos_time.setItem(i, 2, QTableWidgetItem(f"{partida.gols_casa} x {partida.gols_fora}"))
            self.tabela_jogos_time.setItem(i, 3, QTableWidgetItem(partida.time_fora))

    def _atualizar_seletor_times(self, regioes_selecionadas: List[str]):
        if self.analisador is None:
            self.seletor_times_aba_analise.clear()
            return
        
        nomes = self.analisador.get_nomes_dos_times(regioes_selecionadas)
        self.seletor_times_aba_analise.clear()
        self.seletor_times_aba_analise.addItems(nomes)
        
    def _criar_aba_graficos(self):
        aba = QWidget()
        layout = QVBoxLayout(aba)
        
        self.seletor_tipo_grafico = QComboBox()
        self.seletor_tipo_grafico.addItems([
            "Desempenho ao Longo das Rodadas",
            "Gols Marcados vs. Gols Sofridos",
            "Distribuição de Resultados por Time",
            "Distribuição de Total de Gols por Ano",
            "Distribuição do Número Médio de Gols por Jogo",
            "Top 10 Invencibilidade (%)",
            "Top 10 Estádios com Mais Gols",
            "Gols vs Vitórias",
            "Ordenação Títulos menos Rebaixamentos"
        ])
        self.seletor_tipo_grafico.setPlaceholderText("Selecione um tipo de análise gráfica...")
        self.seletor_tipo_grafico.setCurrentIndex(-1)
        
        self.painel_grafico_container = QWidget()
        self.painel_grafico_layout = QVBoxLayout(self.painel_grafico_container)
        
        self.seletor_tipo_grafico.currentIndexChanged.connect(self._atualizar_grafico)
        
        layout.addWidget(self.seletor_tipo_grafico)
        layout.addWidget(self.painel_grafico_container)
        self.tab_widget.addTab(aba, "Gráficos")

    def _get_regioes_selecionadas(self):
        regioes = []
        if self.cb_sudeste.isChecked(): regioes.append("Sudeste")
        if self.cb_sul.isChecked(): regioes.append("Sul")
        if self.cb_nordeste.isChecked(): regioes.append("Nordeste")
        if self.cb_centro_oeste.isChecked(): regioes.append("Centro-Oeste")
        return regioes

    def _limpar_grafico(self):
        for i in reversed(range(self.painel_grafico_layout.count())):
            widget = self.painel_grafico_layout.itemAt(i).widget()
            if widget:
                widget.setParent(None)

    def _limpar_ui(self):
        self.tabela_classificacao.setRowCount(0)
        self.tabela_jogos_time.setRowCount(0)
        self._limpar_grafico()
        
    def _atualizar_grafico(self, index):
        if self.analisador is None or index == -1:
            self._limpar_grafico()
            return
            
        tipo_selecionado = self.seletor_tipo_grafico.currentText()
        regioes_selecionadas = self._get_regioes_selecionadas()
        
        self._limpar_grafico()
        
        ano_inicio = int(self.seletor_ano_inicio.currentText())
        ano_fim = int(self.seletor_ano_fim.currentText())
        num_anos = ano_fim - ano_inicio + 1

        if tipo_selecionado == "Desempenho ao Longo das Rodadas":
            self._criar_grafico_desempenho(regioes_selecionadas)
        elif tipo_selecionado == "Gols Marcados vs. Gols Sofridos":
            self._criar_grafico_dispersao_gols(regioes_selecionadas)
        elif tipo_selecionado == "Distribuição de Resultados por Time":
            self._criar_grafico_pizza_resultados_time(regioes_selecionadas)
        elif tipo_selecionado == "Distribuição de Total de Gols por Ano":
            self._criar_histograma_total_gols(num_anos, regioes_selecionadas)
        elif tipo_selecionado == "Distribuição do Número Médio de Gols por Jogo":
            self._criar_histograma_num_medio_gols_por_jogo(num_anos, regioes_selecionadas)
        elif tipo_selecionado == "Top 10 Invencibilidade (%)":
            self._criar_grafico_invencibilidade(regioes_selecionadas)
        elif tipo_selecionado == "Top 10 Estádios com Mais Gols":
            self._criar_grafico_estadios(regioes_selecionadas)
        elif tipo_selecionado == "Gols vs Vitórias":
            self._criar_grafico_dispersao_gols_vs_vitoria(regioes_selecionadas)
        elif tipo_selecionado == "Ordenação Títulos menos Rebaixamentos":
            self._criar_grafico_historico(regioes_selecionadas)


    def _criar_grafico_desempenho(self, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        sc.axes.set_title("Acúmulo de Pontos por Rodada")
        sc.axes.set_xlabel("Rodada")
        sc.axes.set_ylabel("Pontos Acumulados")
        
        nomes_times = self.analisador.get_nomes_dos_times(regioes_selecionadas)
        series_data = self.analisador.get_performance_ao_longo_das_rodadas(nomes_times)
        
        for nome, data in series_data.items():
            sc.axes.plot(data['x'], data['y'], label=nome)
        
        sc.axes.legend()
        self.painel_grafico_layout.addWidget(sc)

    def _criar_grafico_dispersao_gols_vs_vitoria(self, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        
        sc.axes.set_title("Gols Marcados vs. Vitórias")
        sc.axes.set_xlabel("Gols Marcados")
        sc.axes.set_ylabel("Vitórias")
        
        dados = self.analisador.get_tabela_classificacao(False, regioes_selecionadas)
        

        x = [time.gols_pro for time in dados]
        y = [time.vitorias for time in dados]
        nomes = [time.nome for time in dados]
        
        sc.axes.scatter(x, y)
        for i, nome in enumerate(nomes):
            sc.axes.annotate(nome, (x[i], y[i]))
            
        self.painel_grafico_layout.addWidget(sc)

 
    def _criar_grafico_pizza_resultados_time(self, regioes_selecionadas: List[str]):
        painel_pizza = QWidget()
        layout_pizza = QVBoxLayout(painel_pizza)
        
        combo_time = QComboBox()
        nomes_times = self.analisador.get_nomes_dos_times(regioes_selecionadas)
        combo_time.addItems(nomes_times)
        combo_time.setPlaceholderText("Selecione um time")
        
        sc = MplCanvas(self)
        
        def atualizar_pizza():
            time_selecionado = combo_time.currentText()
            if time_selecionado:
                time = self.analisador.estatisticas_times.get(time_selecionado)
                if time and time.jogos > 0:
                    resultados = [time.vitorias, time.empates, time.derrotas]
                    labels = ['Vitórias', 'Empates', 'Derrotas']
                    
                    sc.axes.clear()
                    sc.axes.pie(resultados, labels=labels, autopct='%1.1f%%', startangle=90)
                    sc.axes.set_title(f"Distribuição de Resultados - {time_selecionado}")
                    sc.draw()

        combo_time.currentIndexChanged.connect(atualizar_pizza)
        
        layout_pizza.addWidget(combo_time)
        layout_pizza.addWidget(sc)
        
        self.painel_grafico_layout.addWidget(painel_pizza)
        if nomes_times:
            combo_time.setCurrentIndex(0)

    def _criar_histograma_total_gols(self, num_anos: int, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        sc.axes.set_title("Distribuição de Total de Gols por Ano")
        sc.axes.set_xlabel("Faixa de Gols Marcados")
        sc.axes.set_ylabel("Número de Times")
        
        dados = self.analisador.get_distribuicao_total_gols(num_anos, regioes_selecionadas)
        labels = list(dados.keys())
        valores = list(dados.values())
        
        sc.axes.bar(labels, valores)
        self.painel_grafico_layout.addWidget(sc)

    def _criar_histograma_num_medio_gols_por_jogo(self, num_anos: int, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        sc.axes.set_title("Distribuição da Média de Gols por Jogo")
        sc.axes.set_xlabel("Médias de Gols")
        sc.axes.set_ylabel("Número de Times")

        dados = self.analisador.get_distribuicao_media_gols(num_anos, regioes_selecionadas)
        labels = list(dados.keys())
        valores = list(dados.values())
        
        sc.axes.bar(labels, valores)
        self.painel_grafico_layout.addWidget(sc)


    def _criar_grafico_invencibilidade(self, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        sc.axes.set_title("Top 10 - Percentual de Invencibilidade")
        sc.axes.set_xlabel("Time")
        sc.axes.set_ylabel("Jogos sem Derrota (%)")
        
        dados = self.analisador.get_percentual_invencibilidade(regioes_selecionadas)
        labels = list(dados.keys())
        valores = list(dados.values())
        
        sc.axes.bar(labels, valores, color='skyblue')
        sc.axes.tick_params(axis='x', rotation=45)
        self.painel_grafico_layout.addWidget(sc)


    def _criar_grafico_estadios(self, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        sc.axes.set_title("Top 10 Estádios com Mais Gols")
        sc.axes.set_xlabel("Total de Gols")
        sc.axes.set_ylabel("Estádios")
        
        dados = self.analisador.get_estadios_com_mais_gols(regioes_selecionadas)
        labels = list(dados.keys())
        valores = list(dados.values())
        
        sc.axes.barh(labels, valores, color='orange')
        sc.axes.invert_yaxis() # Inverte a ordem para que o maior valor fique no topo
        self.painel_grafico_layout.addWidget(sc)


    def _criar_grafico_dispersao_gols(self, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        sc.axes.set_title("Relação entre Gols e Vitórias")
        sc.axes.set_xlabel("Gols Marcados (GM)")
        sc.axes.set_ylabel("Gols Sofridos (GS)")
        
        dados = self.analisador.get_tabela_classificacao(False, regioes_selecionadas)
        x = [time.gols_pro for time in dados]
        y = [time.gols_contra for time in dados]
        nomes = [time.nome for time in dados]
        
        sc.axes.scatter(x, y)
        for i, nome in enumerate(nomes):
            sc.axes.annotate(nome, (x[i], y[i]))
            
        self.painel_grafico_layout.addWidget(sc)
        

    def _criar_grafico_historico(self, regioes_selecionadas: List[str]):
        sc = MplCanvas(self)
        sc.axes.set_title("Histórico de Títulos no Período")
        sc.axes.set_xlabel("Títulos")
        sc.axes.set_ylabel("Time")

        dados = self.analisador.get_historico_titulos_rebaixamentos(regioes_selecionadas)
        
        titulos = {time: dados[time][0] for time in dados if dados[time][0] > 0}
        
        # Pega os 4 maiores valores
        titulos_top_4 = dict(sorted(titulos.items(), key=lambda item: item[1], reverse=True)[:4])

        labels = list(titulos_top_4.keys())
        valores = list(titulos_top_4.values())
        
        sc.axes.barh(labels, valores, color='green')
        sc.axes.invert_yaxis()
        
        self.painel_grafico_layout.addWidget(sc)

    def _atualizar_seletor_times(self, regioes_selecionadas: List[str]):
        if self.analisador is None:
            self.seletor_times_aba_analise.clear()
            return
        
        nomes = self.analisador.get_nomes_dos_times(regioes_selecionadas)
        self.seletor_times_aba_analise.clear()
        self.seletor_times_aba_analise.addItems(nomes)
        
    def _limpar_ui(self):
        self.tabela_classificacao.setRowCount(0)
        self.tabela_jogos_time.setRowCount(0)
        self._limpar_grafico()