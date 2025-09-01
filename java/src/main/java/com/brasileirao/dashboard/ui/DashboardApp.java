package com.brasileirao.dashboard.ui;

import com.brasileirao.dashboard.analise.AnalisadorCampeonato;
import com.brasileirao.dashboard.dados.LeitorCSV;
import com.brasileirao.dashboard.modelo.Partida;
import com.brasileirao.dashboard.modelo.Time;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Imports de imagem e renderização foram removidos para evitar o erro :( não consegui deixar cada time com seu escudo nox gráficos ai mudei o código 
// import java.io.InputStream;
// import javafx.application.Platform;
// import java.text.Normalizer;
// import javafx.scene.layout.StackPane;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.beans.value.ChangeListener;
// import javafx.beans.value.ObservableValue;


public class DashboardApp extends Application {

    private AnalisadorCampeonato analisador;
    private List<Partida> todasAsPartidasDoPeriodo;

    private ComboBox<Integer> seletorAnoInicio, seletorAnoFim;
    private ToggleGroup grupoTurno;
    private CheckBox cbSudeste, cbSul, cbNordeste, cbCentroOeste;
    
    private Label tituloClassificacao;
    private TableView<Time> tabelaClassificacao;
    private ObservableList<Time> dadosTabelaClassificacao;
    private CheckBox checkboxOrdem;
    private TableColumn<Time, Number> colPontos;
    
    private ComboBox<String> seletorTimesAbaAnalise;
    private TableView<Partida> tabelaJogosTime;

    private VBox painelGraficoContainer;
    private ComboBox<String> seletorTipoGrafico;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Dashboard Brasileirão POO");
        BorderPane layoutPrincipal = new BorderPane();

        layoutPrincipal.setTop(criarPainelSuperior());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(criarAbaGraficos(), criarAbaClassificacao(), criarAbaAnaliseTime());
        layoutPrincipal.setCenter(tabPane);
        
        Scene scene = new Scene(layoutPrincipal, 960, 720);
        primaryStage.setScene(scene);
        primaryStage.show();

        carregarDadosDoPeriodo();
    }
    
    private VBox criarPainelSuperior() {
        VBox painelSuperior = new VBox(10);
        painelSuperior.setPadding(new Insets(10));
        
        HBox painelSelecaoAnos = new HBox(10);
        seletorAnoInicio = new ComboBox<>();
        seletorAnoFim = new ComboBox<>();
        for (int ano = 2015; ano <= 2023; ano++) {
            seletorAnoInicio.getItems().add(ano);
            seletorAnoFim.getItems().add(ano);
        }
        seletorAnoInicio.setValue(2023);
        seletorAnoFim.setValue(2023);
        Button btnAnalisar = new Button("Analisar");
        painelSelecaoAnos.getChildren().addAll(new Label("De:"), seletorAnoInicio, new Label("Até:"), seletorAnoFim, btnAnalisar);

        HBox painelFiltros = new HBox(30);
        
        HBox painelFiltroTurno = new HBox(10);
        grupoTurno = new ToggleGroup();
        RadioButton rbCompleto = new RadioButton("Completo"), rbTurno1 = new RadioButton("1º Turno"), rbTurno2 = new RadioButton("2º Turno");
        rbCompleto.setToggleGroup(grupoTurno);
        rbCompleto.setSelected(true);
        rbCompleto.setUserData("COMPLETO");
        rbTurno1.setToggleGroup(grupoTurno);
        rbTurno1.setUserData("TURNO1");
        rbTurno2.setToggleGroup(grupoTurno);
        rbTurno2.setUserData("TURNO2");
        painelFiltroTurno.getChildren().addAll(new Label("Turno:"), rbCompleto, rbTurno1, rbTurno2);

        HBox painelFiltroRegiao = new HBox(10);
        cbSudeste = new CheckBox("Sudeste");
        cbSul = new CheckBox("Sul");
        cbNordeste = new CheckBox("Nordeste");
        cbCentroOeste = new CheckBox("Centro-Oeste");
        
        cbSudeste.setSelected(true);
        cbSul.setSelected(true);
        cbNordeste.setSelected(true);
        cbCentroOeste.setSelected(true);
        
        painelFiltroRegiao.getChildren().addAll(new Label("Filtrar Partidas por Região:"), cbSudeste, cbSul, cbNordeste, cbCentroOeste);
        
        painelFiltros.getChildren().addAll(painelFiltroTurno, painelFiltroRegiao);
        painelSuperior.getChildren().addAll(painelSelecaoAnos, painelFiltros);

        btnAnalisar.setOnAction(e -> carregarDadosDoPeriodo());
        grupoTurno.selectedToggleProperty().addListener((obs, old, aNew) -> filtrarEAtualizarAnalise());
        cbSudeste.setOnAction(e -> filtrarEAtualizarAnalise());
        cbSul.setOnAction(e -> filtrarEAtualizarAnalise());
        cbNordeste.setOnAction(e -> filtrarEAtualizarAnalise());
        cbCentroOeste.setOnAction(e -> filtrarEAtualizarAnalise());
        
        return painelSuperior;
    }

    private void carregarDadosDoPeriodo() {
        int anoInicio = seletorAnoInicio.getValue();
        int anoFim = seletorAnoFim.getValue();
        if (anoFim < anoInicio) {
            new Alert(Alert.AlertType.ERROR, "O ano final não pode ser menor que o ano inicial.").showAndWait();
            return;
        }

        todasAsPartidasDoPeriodo = new ArrayList<>();
        for (int ano = anoInicio; ano <= anoFim; ano++) {
            String nomeArquivo = String.format("brasileirao_%d_completo.csv", ano);
            List<Partida> partidasDoAno = LeitorCSV.carregarPartidas(nomeArquivo);
            todasAsPartidasDoPeriodo.addAll(partidasDoAno);
        }

        if (todasAsPartidasDoPeriodo.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Nenhum dado encontrado para o período selecionado.").showAndWait();
            limparUI();
            return;
        }
        filtrarEAtualizarAnalise();
    }
    
    private void filtrarEAtualizarAnalise() {
        if (todasAsPartidasDoPeriodo == null || todasAsPartidasDoPeriodo.isEmpty()) {
            limparUI();
            return;
        }
        
        List<String> regioesSelecionadas = new ArrayList<>();
        if (cbSudeste.isSelected()) regioesSelecionadas.add("Sudeste");
        if (cbSul.isSelected()) regioesSelecionadas.add("Sul");
        if (cbNordeste.isSelected()) regioesSelecionadas.add("Nordeste");
        if (cbCentroOeste.isSelected()) regioesSelecionadas.add("Centro-Oeste");
        
        List<Partida> partidasPorRegiao = AnalisadorCampeonato.filtrarPorRegioes(todasAsPartidasDoPeriodo, regioesSelecionadas);

        List<Partida> partidasFiltradas = partidasPorRegiao;
        String filtroTurno = (String) grupoTurno.getSelectedToggle().getUserData();
        String textoTituloPeriodo = String.format("%d-%d", seletorAnoInicio.getValue(), seletorAnoFim.getValue());
        if(seletorAnoInicio.getValue().equals(seletorAnoFim.getValue())) {
            textoTituloPeriodo = String.format("%d", seletorAnoInicio.getValue());
        }

        switch (filtroTurno) {
            case "TURNO1":
                partidasFiltradas = partidasPorRegiao.stream()
                        .filter(p -> p.getRodada() <= 19)
                        .collect(Collectors.toList());
                textoTituloPeriodo += " (Apenas 1º Turno)";
                break;
            case "TURNO2":
                partidasFiltradas = partidasPorRegiao.stream()
                        .filter(p -> p.getRodada() > 19)
                        .collect(Collectors.toList());
                textoTituloPeriodo += " (Apenas 2º Turno)";
                break;
        }

        if (partidasFiltradas.isEmpty()) {
            limparUI();
            analisador = null;
            return;
        }
        
        analisador = new AnalisadorCampeonato(partidasFiltradas);
        
        tituloClassificacao.setText("Tabela de Classificação - Brasileirão " + textoTituloPeriodo);
        dadosTabelaClassificacao.setAll(analisador.getTabelaClassificacao(false, regioesSelecionadas));
        checkboxOrdem.setSelected(false);
        if (!tabelaClassificacao.getSortOrder().isEmpty()) {
            tabelaClassificacao.sort();
        } else {
             colPontos.setSortType(TableColumn.SortType.DESCENDING);
             tabelaClassificacao.getSortOrder().setAll(colPontos);
        }
        
        seletorTimesAbaAnalise.setItems(FXCollections.observableArrayList(analisador.getNomesDosTimes(regioesSelecionadas)));
        seletorTimesAbaAnalise.setValue(null);
        tabelaJogosTime.getItems().clear();
        
        atualizarGrafico();
    }
    
    private Tab criarAbaClassificacao() {
        Tab tab = new Tab("Classificação Geral");
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));

        tituloClassificacao = new Label();
        tituloClassificacao.setFont(new Font("Arial", 20));

        checkboxOrdem = new CheckBox("Ver em ordem crescente");
        checkboxOrdem.setOnAction(e -> {
            if (colPontos != null) {
                colPontos.setSortType(checkboxOrdem.isSelected() ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
                tabelaClassificacao.getSortOrder().setAll(colPontos);
            }
        });

        tabelaClassificacao = new TableView<>();
        dadosTabelaClassificacao = FXCollections.observableArrayList();
        configurarTabelaClassificacao();
        tabelaClassificacao.setItems(dadosTabelaClassificacao);
        
        painel.getChildren().addAll(tituloClassificacao, checkboxOrdem, tabelaClassificacao);
        tab.setContent(painel);
        return tab;
    }
    
    private Tab criarAbaAnaliseTime() {
        Tab tab = new Tab("Jogos por Time");
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        
        Label titulo = new Label("Selecione um time para ver seus jogos:");
        titulo.setFont(new Font("Arial", 16));

        seletorTimesAbaAnalise = new ComboBox<>();
        tabelaJogosTime = new TableView<>();
        configurarTabelaPartidas(tabelaJogosTime);

        seletorTimesAbaAnalise.setOnAction(e -> {
            String timeSelecionado = seletorTimesAbaAnalise.getValue();
            if (timeSelecionado != null && analisador != null) {
                List<Partida> jogosDoTime = analisador.getPartidasPorTime(timeSelecionado);
                tabelaJogosTime.setItems(FXCollections.observableArrayList(jogosDoTime));
            }
        });
        
        painel.getChildren().addAll(titulo, seletorTimesAbaAnalise, tabelaJogosTime);
        tab.setContent(painel);
        return tab;
    }

    private Tab criarAbaGraficos() {
        Tab tab = new Tab("Gráficos");
        VBox painelPrincipal = new VBox(10);
        painelPrincipal.setPadding(new Insets(10));
        
        seletorTipoGrafico = new ComboBox<>();
        seletorTipoGrafico.getItems().addAll(
            "Desempenho ao Longo das Rodadas",
            "Gols Marcados vs. Gols Sofridos",
            "Distribuição de Resultados por Time",
            "Distribuição de Total de Gols por Ano",
            "Distribuição do Número Médio de Gols por Jogo",            
            "Top Clubes por Invencibilidade (%)",
            "Top Estádios com Mais Gols",
            "Número de Gols vs % de vitórias",
            "Aparições no top 4"
        );
        seletorTipoGrafico.setPromptText("Selecione um tipo de análise gráfica...");
        
        painelGraficoContainer = new VBox();

        seletorTipoGrafico.setOnAction(e -> atualizarGrafico());
        
        painelPrincipal.getChildren().addAll(seletorTipoGrafico, painelGraficoContainer);
        tab.setContent(painelPrincipal);
        return tab;
    }

    private void atualizarGrafico() {
        if (analisador == null || seletorTipoGrafico.getValue() == null) {
            painelGraficoContainer.getChildren().clear();
            return;
        }

        int anoInicio = seletorAnoInicio.getValue();
        int anoFim = seletorAnoFim.getValue();
        int numAnos = anoFim - anoInicio + 1;
        if (numAnos <= 0){
            new Alert(Alert.AlertType.ERROR, "O ano final não pode ser menor que o ano inicial.").showAndWait();
            return;
        }

        List<String> regioesGrafico = new ArrayList<>();
        if (cbSudeste.isSelected()) regioesGrafico.add("Sudeste");
        if (cbSul.isSelected()) regioesGrafico.add("Sul");
        if (cbNordeste.isSelected()) regioesGrafico.add("Nordeste");
        if (cbCentroOeste.isSelected()) regioesGrafico.add("Centro-Oeste");
        
        String tipoSelecionado = seletorTipoGrafico.getValue();
        Node grafico = new Label("Gráfico a ser implementado.");

        switch (tipoSelecionado) {
            case "Desempenho ao Longo das Rodadas":
                grafico = criarPainelGraficoDesempenho(regioesGrafico);
                break;
            case "Gols Marcados vs. Gols Sofridos":
                // 
                grafico = criarGraficoDispersaoGols(regioesGrafico);
                break;
            case "Distribuição de Resultados por Time":
                grafico = criarGraficoPizzaResultadosTime(regioesGrafico);
                break;
            case "Distribuição de Total de Gols por Ano":
                grafico = criarHistogramaTotalGols(numAnos, regioesGrafico);
                break;
            case "Distribuição do Número Médio de Gols por Jogo":
                grafico = criarHistogramaNumMedioGolsPorJogo(numAnos, regioesGrafico);
                break;
            case "Top Clubes por Invencibilidade (%)":
                grafico = criarGraficoInvencibilidade(regioesGrafico);
                break;
            case "Top Estádios com Mais Gols":
                grafico = criarGraficoEstadios(regioesGrafico);
                break;
            case "Número de Gols vs % de vitórias":
                grafico = criarGraficoDispersaoGolsVsVitoria(regioesGrafico);
                break;
            case "Aparições no top 4":
                grafico = criarGraficoHistorico(regioesGrafico);
                break;
        }
        painelGraficoContainer.getChildren().setAll(grafico);
    }
    
    private Node criarPainelGraficoDesempenho(List<String> regioesSelecionadas) { //considera o acumulo até a rodada somando o de todos campeonatos selecionados
        VBox painelPrincipal = new VBox(10);
        painelPrincipal.setPadding(new Insets(5));
        Label labelSelecao = new Label("Selecione os times para comparar:");
        VBox painelCheckboxes = new VBox(5);
        List<CheckBox> listaDeCheckboxes = new ArrayList<>();
        List<String> nomesDosTimesFiltrados = analisador.getNomesDosTimes(regioesSelecionadas);
        for (String nomeTime : nomesDosTimesFiltrados) {
            CheckBox cb = new CheckBox(nomeTime);
            cb.setSelected(true);
            listaDeCheckboxes.add(cb);
            painelCheckboxes.getChildren().add(cb);
        }
        ScrollPane scrollPane = new ScrollPane(painelCheckboxes);
        scrollPane.setPrefHeight(150);
        scrollPane.setFitToWidth(true);
        HBox painelBotoes = new HBox(10);
        Button btnSelecionarTodos = new Button("Selecionar Todos");
        Button btnLimparSelecao = new Button("Limpar Seleção");
        painelBotoes.getChildren().addAll(btnSelecionarTodos, btnLimparSelecao);
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Rodada");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Pontos Acumulados");
        LineChart<Number, Number> grafico = new LineChart<>(xAxis, yAxis);
        grafico.setTitle("Acúmulo de Pontos por Rodada");
        Runnable atualizadorDoGrafico = () -> {
            List<String> timesSelecionados = listaDeCheckboxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .collect(Collectors.toList());
            if (!timesSelecionados.isEmpty()) {
                Map<String, XYChart.Series<Number, Number>> seriesMap = analisador.getPerformanceAoLongoDasRodadas(timesSelecionados);
                grafico.getData().setAll(seriesMap.values());
            } else {
                grafico.getData().clear();
            }
        };
        listaDeCheckboxes.forEach(cb -> cb.setOnAction(e -> atualizadorDoGrafico.run()));
        btnSelecionarTodos.setOnAction(e -> {
            listaDeCheckboxes.forEach(cb -> cb.setSelected(true));
            atualizadorDoGrafico.run();
        });
        btnLimparSelecao.setOnAction(e -> {
            listaDeCheckboxes.forEach(cb -> cb.setSelected(false));
            atualizadorDoGrafico.run();
        });
        painelPrincipal.getChildren().addAll(labelSelecao, scrollPane, painelBotoes, grafico);
        atualizadorDoGrafico.run();
        return painelPrincipal;
    }

    private Node criarGraficoDispersaoGols(List<String> regioesSelecionadas) {
        
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Gols Marcados (GM)");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Gols Sofridos (GS)");
        ScatterChart<Number, Number> grafico = new ScatterChart<>(xAxis, yAxis);
        grafico.setTitle("Desempenho Ofensivo vs. Defensivo");
        
        List<Time> timesParaExibir = analisador.getTabelaClassificacao(false, regioesSelecionadas);

        for (Time time : timesParaExibir) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(time.getNome());
            series.getData().add(new XYChart.Data<>(time.getGolsMarc(), time.getGolsSofr()));
            grafico.getData().add(series);
        }
        return grafico;
    }
    
    private Node criarGraficoPizzaResultadosTime(List<String> regioesSelecionadas) {
        VBox painel = new VBox(10);
        List<String> nomesTimesFiltrados = analisador.getNomesDosTimes(regioesSelecionadas);
        ComboBox<String> comboTime = new ComboBox<>(FXCollections.observableArrayList(nomesTimesFiltrados));
        comboTime.setPromptText("Selecione um time");
        PieChart grafico = new PieChart();
        grafico.setTitle("Distribuição de Resultados");
        grafico.setClockwise(false);
        comboTime.setOnAction(e -> {
            Time time = analisador.getEstatisticasTime(comboTime.getValue());
            if (time != null && time.getJogos() > 0) {
                ObservableList<PieChart.Data> pieChartData =
                        FXCollections.observableArrayList(
                                new PieChart.Data("Vitórias", time.getVitorias()),
                                new PieChart.Data("Empates", time.getEmpates()),
                                new PieChart.Data("Derrotas", time.getDerrotas()));
                grafico.setData(pieChartData);
                double total = time.getJogos();
                pieChartData.forEach(data -> 
                    data.setName(String.format("%s (%.1f%%)", data.getName(), (data.getPieValue() / total) * 100))
                );
            } else {
                grafico.setData(FXCollections.observableArrayList());
            }
        });
        painel.getChildren().addAll(comboTime, grafico);
        return painel;
    }
    private Node criarHistogramaTotalGols(int numAnos, List<String> regioesSelecionadas) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Faixa de Gols Marcados por Ano");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Número de Times");
        BarChart<String, Number> grafico = new BarChart<>(xAxis, yAxis);
        grafico.setTitle("Histograma de Total de Gols");
        grafico.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> dados = analisador.getDistribuicaoTotalGols(numAnos, regioesSelecionadas);
        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        grafico.getData().add(series);
        return grafico;
    }
    private Node criarHistogramaNumMedioGolsPorJogo(int numAnos, List<String> regioesSelecionadas){
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Médias de Gols");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Número de Times");
        BarChart<String, Number> grafico = new BarChart<>(xAxis, yAxis);
        grafico.setTitle("Histograma da média de Gols");
        grafico.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> dados = analisador.getDistribuicaoMediaGols(numAnos, regioesSelecionadas);
        for (Map.Entry<String, Double> entry : dados.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        grafico.getData().add(series);
        return grafico;
    }

    private Node criarGraficoInvencibilidade(List<String> regioesSelecionadas) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jogos sem Derrota (%)");
        BarChart<String, Number> grafico = new BarChart<>(xAxis, yAxis);
        grafico.setTitle("Top  - Percentual de Invencibilidade");
        grafico.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> dados = analisador.getPercentualInvencibilidade(regioesSelecionadas);
        dados.entrySet().forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
        grafico.getData().add(series);
        return grafico;
    }

    private Node criarGraficoEstadios(List<String> regioesSelecionadas) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Estádios");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total de Gols");
        BarChart<String, Number> grafico = new BarChart<>(xAxis, yAxis);
        grafico.setTitle("Top Estádios com Mais Gols");
        grafico.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Long> dados = analisador.getEstadiosComMaisGols(regioesSelecionadas);
        for (Map.Entry<String, Long> entry : dados.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        grafico.getData().add(series);
        return grafico;
    }


    private Node criarGraficoDispersaoGolsVsVitoria(List<String> regioesSelecionadas) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Gols Marcados (GM)");
        NumberAxis yAxis = new NumberAxis();
        // ** MUDANÇA: A lógica foi ajustada para usar a porcentagem de vitórias **
        yAxis.setLabel("Porcentagem de Vitórias (%)");
        ScatterChart<Number, Number> grafico = new ScatterChart<>(xAxis, yAxis);
        grafico.setTitle("Relação entre Gols Marcados e Porcentagem de Vitórias");
        grafico.setLegendVisible(false); // Remove a legenda para um gráfico mais limpo
        
        // ** MUDANÇA: A lógica foi ajustada para usar a porcentagem de vitórias **
        List<Time> timesParaExibir = analisador.getTabelaClassificacao(false, regioesSelecionadas);

        for (Time time : timesParaExibir) {
            if (time.getJogos() > 0) {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(time.getNome());
                double percentualVitorias = ((double) time.getVitorias() / time.getJogos()) * 100;
                series.getData().add(new XYChart.Data<>(time.getGolsMarc(), percentualVitorias));
                grafico.getData().add(series);
            }
        }
        return grafico;
    }


    private Node criarGraficoHistorico(List<String> regioesSelecionadas) {
        CategoryAxis yAxis = new CategoryAxis();
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Aparições no top 4 ");
        
        BarChart<Number, String> grafico = new BarChart<>(xAxis, yAxis);
        grafico.setTitle("Histórico de top4 no Período");
        
        
        XYChart.Series<Number, String> topSeries = new XYChart.Series<>();
        topSeries.setName("vezes no top 4");
        
        Map<String, int[]> dados = analisador.getHistorico(regioesSelecionadas);
        
        dados.entrySet().forEach(entry -> {
            String time = entry.getKey();
            int aparicoesTop4 = entry.getValue()[1];
            
            if (aparicoesTop4 > 0) {
                topSeries.getData().add(new XYChart.Data<>(-aparicoesTop4, time));
            }
        });

        grafico.getData().add(topSeries);

        topSeries.nodeProperty().addListener((obs, old, aNew) -> {
            if(aNew != null) aNew.setStyle("-fx-bar-fill: #B22222;");
        });
        
        return grafico;
    }

    private void configurarTabelaClassificacao() {
        TableColumn<Time, String> colNome = new TableColumn<>("Time");
        colNome.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        colPontos = new TableColumn<>("P");
        colPontos.setCellValueFactory(cellData -> cellData.getValue().pontosProperty());
        TableColumn<Time, Number> colJogos = new TableColumn<>("J");
        colJogos.setCellValueFactory(cellData -> cellData.getValue().jogosProperty());
        TableColumn<Time, Number> colVitorias = new TableColumn<>("V");
        colVitorias.setCellValueFactory(cellData -> cellData.getValue().vitoriasProperty());
        TableColumn<Time, Number> colEmpates = new TableColumn<>("E");
        colEmpates.setCellValueFactory(cellData -> cellData.getValue().empatesProperty());
        TableColumn<Time, Number> colDerrotas = new TableColumn<>("D");
        colDerrotas.setCellValueFactory(cellData -> cellData.getValue().derrotasProperty());
        TableColumn<Time, Number> colGolsPro = new TableColumn<>("GM");
        colGolsPro.setCellValueFactory(cellData -> cellData.getValue().golsProProperty());
        TableColumn<Time, Number> colGolsContra = new TableColumn<>("GS");
        colGolsContra.setCellValueFactory(cellData -> cellData.getValue().golsContraProperty());
        TableColumn<Time, Number> colSaldoGols = new TableColumn<>("SG");
        colSaldoGols.setCellValueFactory(cellData -> cellData.getValue().saldoGolsProperty());
        tabelaClassificacao.getColumns().addAll(colNome, colPontos, colJogos, colVitorias, colEmpates, colDerrotas, colGolsPro, colGolsContra, colSaldoGols);
    }

    private void configurarTabelaPartidas(TableView<Partida> tabela) {
        TableColumn<Partida, Integer> colRodada = new TableColumn<>("Rodada");
        colRodada.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getRodada()).asObject());
        TableColumn<Partida, String> colCasa = new TableColumn<>("Time Casa");
        colCasa.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimeCasa()));
        TableColumn<Partida, String> colPlacar = new TableColumn<>("Placar");
        colPlacar.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getGolsCasa() + " x " + cellData.getValue().getGolsFora()
        ));
        TableColumn<Partida, String> colFora = new TableColumn<>("Time Fora");
        colFora.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimeFora()));
        tabela.getColumns().addAll(colRodada, colCasa, colPlacar, colFora);
    }
    
    private void limparUI() {
        dadosTabelaClassificacao.clear();
        tituloClassificacao.setText("Tabela de Classificação - Nenhum dado disponível");
        
        seletorTimesAbaAnalise.setItems(FXCollections.observableArrayList());
        seletorTimesAbaAnalise.setValue(null);
        tabelaJogosTime.getItems().clear();
        
        painelGraficoContainer.getChildren().setAll(new Label("Selecione um período com dados válidos."));
    }

    public static void main(String[] args) {
        launch(args);
    }
}