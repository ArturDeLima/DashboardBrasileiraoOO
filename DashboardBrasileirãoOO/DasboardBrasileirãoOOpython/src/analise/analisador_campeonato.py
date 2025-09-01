import pandas as pd
from typing import List, Dict
from modelo.time import Time
from modelo.partida import Partida

class AnalisadorCampeonato:
    TIME_REGIAO_MAP = {
        "Flamengo": "Sudeste", "Fluminense": "Sudeste", "Vasco da Gama": "Sudeste", "Botafogo": "Sudeste",
        "Corinthians": "Sudeste", "Palmeiras": "Sudeste", "São Paulo": "Sudeste", "Santos": "Sudeste",
        "Grêmio": "Sul", "Internacional": "Sul", "Athletico-PR": "Sul", "Coritiba": "Sul",
        "Bahia": "Nordeste", "Fortaleza": "Nordeste", "Atlético-MG": "Sudeste", "Cruzeiro": "Sudeste",
        "América-MG": "Sudeste", "Goiás": "Centro-Oeste", "Cuiabá": "Centro-Oeste", "Bragantino": "Sudeste",
        "Paraná": "Sul", "Joinville": "Sul", "Santa Cruz": "Nordeste", "CSA": "Nordeste",
        "Juventude": "Sul", "Figueirense": "Sul", "EC Vitória": "Nordeste", "Ponte Preta": "Sul",
        "Avaí": "Sudeste", "Atlético-GO": "Centro-Oeste", "Chapecoense": "Sul", "Ceará SC": "Nordeste",
        "Sport Recife": "Nordeste"
    }

    def __init__(self, partidas_df: pd.DataFrame):
        self.partidas_df = partidas_df
        self.estatisticas_times = self._processar_partidas()

    def _processar_partidas(self) -> Dict[str, Time]:
        estatisticas = {}
        for _, row in self.partidas_df.iterrows():
            time_casa_nome = row['time_casa'].strip()
            time_fora_nome = row['time_fora'].strip()

            if time_casa_nome not in estatisticas:
                estatisticas[time_casa_nome] = Time(time_casa_nome)
            if time_fora_nome not in estatisticas:
                estatisticas[time_fora_nome] = Time(time_fora_nome)

            time_casa = estatisticas[time_casa_nome]
            time_fora = estatisticas[time_fora_nome]

            if row['gols_casa'] > row['gols_fora']:
                time_casa.adicionar_vitoria(row['gols_casa'], row['gols_fora'])
                time_fora.adicionar_derrota(row['gols_fora'], row['gols_casa'])
            elif row['gols_fora'] > row['gols_casa']:
                time_fora.adicionar_vitoria(row['gols_fora'], row['gols_casa'])
                time_casa.adicionar_derrota(row['gols_casa'], row['gols_fora'])
            else:
                time_casa.adicionar_empate(row['gols_casa'])
                time_fora.adicionar_empate(row['gols_fora'])
        return estatisticas

    @staticmethod
    def get_regiao_do_time(nome_time: str) -> str:
        return AnalisadorCampeonato.TIME_REGIAO_MAP.get(nome_time, "Desconhecida")

    def get_tabela_classificacao(self, ordem_crescente: bool, regioes_selecionadas: List[str]) -> List[Time]:
        times_filtrados = [
            time for time in self.estatisticas_times.values()
            if not regioes_selecionadas or self.get_regiao_do_time(time.nome) in regioes_selecionadas
        ]
        
        times_filtrados.sort(key=lambda t: (t.pontos, t.vitorias, t.saldo_gols, t.gols_pro), reverse=not ordem_crescente)
        return times_filtrados

    def get_nomes_dos_times(self, regioes_selecionadas: List[str]) -> List[str]:
        return sorted([
            nome for nome in self.estatisticas_times.keys()
            if not regioes_selecionadas or self.get_regiao_do_time(nome) in regioes_selecionadas
        ])
        
    def get_partidas_por_time(self, nome_time: str) -> List[Partida]:
        partidas_do_time = self.partidas_df[
            (self.partidas_df['time_casa'].str.strip() == nome_time) | 
            (self.partidas_df['time_fora'].str.strip() == nome_time)
        ]
        return [Partida(**row) for _, row in partidas_do_time.iterrows()]

    def get_performance_ao_longo_das_rodadas(self, nomes_times: List[str]):
        series_por_time = {nome: {"x": [], "y": []} for nome in nomes_times}
        pontos_acumulados = {nome: 0 for nome in nomes_times}
        
        partidas_ordenadas = self.partidas_df.sort_values(by=['rodada'])

        for _, p in partidas_ordenadas.iterrows():
            time_casa = p['time_casa'].strip()
            time_fora = p['time_fora'].strip()

            pontos_ganhos_casa = 0
            pontos_ganhos_fora = 0
            
            if p['gols_casa'] > p['gols_fora']:
                pontos_ganhos_casa = 3
            elif p['gols_fora'] > p['gols_casa']:
                pontos_ganhos_fora = 3
            else:
                pontos_ganhos_casa = 1
                pontos_ganhos_fora = 1
            
            if time_casa in nomes_times:
                pontos_acumulados[time_casa] += pontos_ganhos_casa
                series_por_time[time_casa]["x"].append(p['rodada'])
                series_por_time[time_casa]["y"].append(pontos_acumulados[time_casa])
            
            if time_fora in nomes_times:
                pontos_acumulados[time_fora] += pontos_ganhos_fora
                series_por_time[time_fora]["x"].append(p['rodada'])
                series_por_time[time_fora]["y"].append(pontos_acumulados[time_fora])
                
        return series_por_time

    def get_distribuicao_total_gols(self, num_anos: int, regioes_selecionadas: List[str]):
        distribuicao = {"0-30": 0, "31-40": 0, "41-50": 0, "51-60": 0, "61+": 0}
        times_filtrados = [
            time for time in self.estatisticas_times.values()
            if not regioes_selecionadas or self.get_regiao_do_time(time.nome) in regioes_selecionadas
        ]

        for time in times_filtrados:
            gols_por_ano = time.gols_pro // num_anos
            if gols_por_ano <= 30: distribuicao["0-30"] += 1
            elif gols_por_ano <= 40: distribuicao["31-40"] += 1
            elif gols_por_ano <= 50: distribuicao["41-50"] += 1
            elif gols_por_ano <= 60: distribuicao["51-60"] += 1
            else: distribuicao["61+"] += 1
        return distribuicao

    def get_distribuicao_media_gols(self, num_anos: int, regioes_selecionadas: List[str]):
        distribuicao = {"0,0-0,3": 0, "0,3-0,6": 0, "0,6-0,9": 0, "0,9-1,2": 0, "1,2+": 0}
        times_filtrados = [
            time for time in self.estatisticas_times.values()
            if not regioes_selecionadas or self.get_regiao_do_time(time.nome) in regioes_selecionadas
        ]

        for time in times_filtrados:
            media_gols_time = time.gols_pro / (num_anos * 38)
            if media_gols_time <= 0.3: distribuicao["0,0-0,3"] += 1
            elif media_gols_time <= 0.6: distribuicao["0,3-0,6"] += 1
            elif media_gols_time <= 0.9: distribuicao["0,6-0,9"] += 1
            elif media_gols_time <= 1.2: distribuicao["0,9-1,2"] += 1
            else: distribuicao["1,2+"] += 1
        return distribuicao

    def get_percentual_invencibilidade(self, regioes_selecionadas: List[str]):
        invencibilidade = {}
        times_filtrados = [
            time for time in self.estatisticas_times.values()
            if not regioes_selecionadas or self.get_regiao_do_time(time.nome) in regioes_selecionadas
        ]
        for time in times_filtrados:
            if time.jogos > 0:
                percentual = ((time.vitorias + time.empates) / time.jogos) * 100
                invencibilidade[time.nome] = percentual
        return dict(sorted(invencibilidade.items(), key=lambda item: item[1], reverse=True)[:10])

    def get_estadios_com_mais_gols(self, regioes_selecionadas: List[str]):
        estadios = self.partidas_df[
            self.partidas_df.apply(
                lambda row: (self.get_regiao_do_time(row['time_casa']) in regioes_selecionadas) or
                            (self.get_regiao_do_time(row['time_fora']) in regioes_selecionadas),
                axis=1
            )
        ]
        estadios['gols_totais'] = estadios['gols_casa'] + estadios['gols_fora']
        estadios_agrupados = estadios.groupby('estadio')['gols_totais'].sum().nlargest(10)
        return estadios_agrupados.to_dict()

    def get_historico_titulos_rebaixamentos(self, regioes_selecionadas: List[str]):
        historico = {}
        partidas_por_ano = self.partidas_df.groupby('ano')
        for ano, df_ano in partidas_por_ano:
            analisador_do_ano = AnalisadorCampeonato(df_ano)
            classificacao_anual = analisador_do_ano.get_tabela_classificacao(False, regioes_selecionadas)
            
            if not classificacao_anual:
                continue
            
            campeao = classificacao_anual[0]
            if campeao.nome not in historico: historico[campeao.nome] = [0, 0]
            historico[campeao.nome][0] += 1
            
            total_times = len(classificacao_anual)
            if total_times >= 20:
                for i in range(4):
                    rebaixado = classificacao_anual[total_times - 1 - i]
                    if rebaixado.nome not in historico: historico[rebaixado.nome] = [0, 0]
                    historico[rebaixado.nome][1] += 1
        return historico

    @staticmethod
    def filtrar_por_regioes(partidas_df: pd.DataFrame, regioes: List[str]):
        if not regioes:
            return partidas_df
        
        return partidas_df[
            partidas_df.apply(
                lambda row: (AnalisadorCampeonato.get_regiao_do_time(row['time_casa']) in regioes) or
                            (AnalisadorCampeonato.get_regiao_do_time(row['time_fora']) in regioes),
                axis=1
            )
        ]