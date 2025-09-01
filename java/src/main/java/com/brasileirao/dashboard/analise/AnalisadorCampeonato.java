package com.brasileirao.dashboard.analise;

import com.brasileirao.dashboard.modelo.Partida;
import com.brasileirao.dashboard.modelo.Time;
import com.brasileirao.dashboard.ui.DashboardApp;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.stream.Collectors;

public class AnalisadorCampeonato {

    private final List<Partida> partidas;
    private final Map<String, Time> estatisticasTimes;
    private final Comparator<Time> comparadorDecrescente;
    private final Comparator<Time> comparadorCrescente;

    private static final Map<String, String> TIME_REGIAO_MAP = Map.ofEntries(
            Map.entry("Flamengo", "Sudeste"), 
            Map.entry("Fluminense", "Sudeste"),
            Map.entry("Vasco da Gama", "Sudeste"), 
            Map.entry("Botafogo", "Sudeste"),
            Map.entry("Corinthians", "Sudeste"), 
            Map.entry("Palmeiras", "Sudeste"),
            Map.entry("São Paulo", "Sudeste"), 
            Map.entry("Santos", "Sudeste"),
            Map.entry("Grêmio", "Sul"), 
            Map.entry("Internacional", "Sul"),
            Map.entry("Athletico-PR", "Sul"), 
            Map.entry("Coritiba", "Sul"),
            Map.entry("Bahia", "Nordeste"), 
            Map.entry("Fortaleza", "Nordeste"),
            Map.entry("Atlético-MG", "Sudeste"), 
            Map.entry("Cruzeiro", "Sudeste"),
            Map.entry("América-MG", "Sudeste"), 
            Map.entry("Goiás", "Centro-Oeste"),
            Map.entry("Cuiabá", "Centro-Oeste"), 
            Map.entry("Bragantino", "Sudeste"),
            Map.entry("Paraná", "Sul"),
            Map.entry("Joinville", "Sul"),
            Map.entry("Santa Cruz", "Nordeste"),
            Map.entry("CSA", "Nordeste"),
            Map.entry("Juventude", "Sul"),
            Map.entry("Figueirense", "Sul"),
            Map.entry("EC Vitória", "Nordeste"),
            Map.entry("Ponte Preta", "Sul"),
            Map.entry("Avaí", "Sudeste"),
            Map.entry("Atlético-GO", "Centro-Oeste"),
            Map.entry("Chapecoense", "Sul"),
            Map.entry("Ceará SC", "Nordeste"),
            Map.entry("Sport Recife", "Nordeste")
    );

    public AnalisadorCampeonato(List<Partida> partidas) {
        this.partidas = partidas;
        this.estatisticasTimes = new HashMap<>();

        this.comparadorDecrescente = Comparator.comparingInt(Time::getPontos).reversed()
                .thenComparingInt(Time::getVitorias).reversed()
                .thenComparingInt(Time::getSaldoGols).reversed()
                .thenComparingInt(Time::getGolsMarc).reversed();
        this.comparadorCrescente = Comparator.comparingInt(Time::getPontos)
                .thenComparingInt(Time::getVitorias)
                .thenComparingInt(Time::getSaldoGols)
                .thenComparingInt(Time::getGolsMarc);
        processarPartidas();
    }

    private void processarPartidas() {
        //Adiciona uma verificação de segurança antes de processar os times 
        // para previnir um NullpointerException se o nome do time for null ou vazio
        for (Partida p : partidas) {
            if (p.getTimeCasa() != null && !p.getTimeCasa().isEmpty()) {
                estatisticasTimes.putIfAbsent(p.getTimeCasa(), new Time(p.getTimeCasa()));
            }
            if (p.getTimeFora() != null && !p.getTimeFora().isEmpty()) {
                estatisticasTimes.putIfAbsent(p.getTimeFora(), new Time(p.getTimeFora()));
            }
        }
        for (Partida p : partidas) {
            Time timeCasa = estatisticasTimes.get(p.getTimeCasa());
            Time timeFora = estatisticasTimes.get(p.getTimeFora());
            // ver se time fora e casa são válidos antes de adicionar 
            if (timeCasa != null && timeFora != null) {
                if (p.getGolsCasa() > p.getGolsFora()) {
                    timeCasa.adicionarVitoria(p.getGolsCasa(), p.getGolsFora());
                    timeFora.adicionarDerrota(p.getGolsFora(), p.getGolsCasa());
                } else if (p.getGolsFora() > p.getGolsCasa()) {
                    timeFora.adicionarVitoria(p.getGolsFora(), p.getGolsCasa());
                    timeCasa.adicionarDerrota(p.getGolsCasa(), p.getGolsFora());
                } else {
                    timeCasa.adicionarEmpate(p.getGolsCasa());
                    timeFora.adicionarEmpate(p.getGolsFora());
                }
            }
        }
    }

    public static String getRegiaoDoTime(String nomeTime) {
        return TIME_REGIAO_MAP.getOrDefault(nomeTime, "Desconhecida");
    }

    public List<Time> getTabelaClassificacao(boolean ordemCrescente, List<String> regioesSelecionadas) {
        return estatisticasTimes.values().stream()
                .filter(time -> regioesSelecionadas.isEmpty() || regioesSelecionadas.contains(getRegiaoDoTime(time.getNome())))
                .sorted(ordemCrescente ? comparadorCrescente : comparadorDecrescente)
                .collect(Collectors.toList());
    }

    public List<String> getNomesDosTimes(List<String> regioesSelecionadas) {
        return estatisticasTimes.keySet().stream()
                .filter(nome -> regioesSelecionadas.isEmpty() || regioesSelecionadas.contains(getRegiaoDoTime(nome)))
                .sorted().collect(Collectors.toList());
    }

    public List<Partida> getPartidasPorTime(String nomeTime) {
        return partidas.stream()
                .filter(p -> p.getTimeCasa().equals(nomeTime) || p.getTimeFora().equals(nomeTime))
                .collect(Collectors.toList());
    }
    
    public List<XYChart.Series<Number, Number>> getSeriesGolsMarcadosVsSofridos(List<String> regioesSelecionadas) {
        return estatisticasTimes.values().stream()
            .filter(time -> regioesSelecionadas.isEmpty() || regioesSelecionadas.contains(getRegiaoDoTime(time.getNome())))
            .map(time -> {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(time.getNome());
                series.getData().add(new XYChart.Data<>(time.getGolsMarc(), time.getGolsSofr()));
                return series;
            })
            .collect(Collectors.toList());
    }
    
    public Map<String, XYChart.Series<Number, Number>> getPerformanceAoLongoDasRodadas(List<String> nomesTimes) {
        Map<String, XYChart.Series<Number, Number>> seriesPorTime = new HashMap<>();
        Map<String, Integer> pontosAcumulados = new HashMap<>();
        
        for (String nomeTime : nomesTimes) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(nomeTime);
            seriesPorTime.put(nomeTime, series);
            pontosAcumulados.put(nomeTime, 0);
            series.getData().add(new XYChart.Data<>(0, 0));
        }

        List<Partida> partidasOrdenadas = partidas.stream()
                .sorted(Comparator.comparingInt(Partida::getRodada))
                .collect(Collectors.toList());

        for (Partida p : partidasOrdenadas) {
            String timeCasa = p.getTimeCasa();
            String timeFora = p.getTimeFora();
            boolean timeCasaSelecionado = nomesTimes.contains(timeCasa);
            boolean timeForaSelecionado = nomesTimes.contains(timeFora);

            if (timeCasaSelecionado || timeForaSelecionado) {
                int pontosGanhosCasa = 0;
                int pontosGanhosFora = 0;

                if (p.getGolsCasa() > p.getGolsFora()) {
                    pontosGanhosCasa = 3;
                } else if (p.getGolsFora() > p.getGolsCasa()) {
                    pontosGanhosFora = 3;
                } else {
                    pontosGanhosCasa = 1;
                    pontosGanhosFora = 1;
                }
                
                if(timeCasaSelecionado) {
                    pontosAcumulados.merge(timeCasa, pontosGanhosCasa, Integer::sum);
                }
                if(timeForaSelecionado) {
                    pontosAcumulados.merge(timeFora, pontosGanhosFora, Integer::sum);
                }

                if(timeCasaSelecionado) {
                    seriesPorTime.get(timeCasa).getData().add(new XYChart.Data<>(p.getRodada(), pontosAcumulados.get(timeCasa)));
                }
                if(timeForaSelecionado) {
                    seriesPorTime.get(timeFora).getData().add(new XYChart.Data<>(p.getRodada(), pontosAcumulados.get(timeFora)));
                }
            }
        }
        return seriesPorTime;
    }

    public Map<String, Integer> getDistribuicaoTotalGols(int numAnos, List<String> regioesSelecionadas) {
        Map<String, Integer> distribuicao = new LinkedHashMap<>();
        distribuicao.put("0-30", 0);
        distribuicao.put("31-40", 0);
        distribuicao.put("41-50", 0);
        distribuicao.put("51-60", 0);
        distribuicao.put("61+", 0);

        List<Time> timesFiltrados = estatisticasTimes.values().stream()
                .filter(time -> regioesSelecionadas.isEmpty() || regioesSelecionadas.contains(getRegiaoDoTime(time.getNome())))
                .collect(Collectors.toList());
        
        for (Time time : timesFiltrados) {
            int gols = time.getGolsMarc();
            int golsPorAno = gols/numAnos;
            if (golsPorAno <= 30) distribuicao.merge("0-30", 1, Integer::sum);
            else if (golsPorAno <= 40) distribuicao.merge("31-40", 1, Integer::sum);
            else if (golsPorAno <= 50) distribuicao.merge("41-50", 1, Integer::sum);
            else if (golsPorAno <= 60) distribuicao.merge("51-60", 1, Integer::sum);
            else distribuicao.merge("61+", 1, Integer::sum);
        }
        return distribuicao;
    }

    public Map<String, Double> getDistribuicaoMediaGols(int numAnos, List<String> regioesSelecionadas){
        Map<String, Double> distribuicaoMedia = new LinkedHashMap<>();
        distribuicaoMedia.put("0,0-0,3", 0.0); 
        distribuicaoMedia.put("0,3-0,6", 0.0);
        distribuicaoMedia.put("0,6-0,9", 0.0);
        distribuicaoMedia.put("0,9-1,2", 0.0);
        distribuicaoMedia.put("1,2+", 0.0);

        List<Time> timesFiltrados = estatisticasTimes.values().stream()
                .filter(time -> regioesSelecionadas.isEmpty() || regioesSelecionadas.contains(getRegiaoDoTime(time.getNome())))
                .collect(Collectors.toList());



/*         double auxClasses = 0.0;
        for (Time time : timesFiltrados){                     // usando alguns princípios de estatística para divisão de classe, com essa form pois os nums são relativamente baixos
            int gols = time.getGolsMarc();                    // não foi feito dessa forma para o método e seu antecessor anterior pois fiz os teste e achei q não era preciso pela forma de suas distribuições
            double mediaGolsTime = (double) (gols / (numAnos * 38));
            if(auxClasses < mediaGolsTime) auxClasses = mediaGolsTime;
        } */
        
        
        for (Time time : timesFiltrados) {
            int gols = time.getGolsMarc();
            double mediaGolsTime = (double) gols / (numAnos * 38); // quando o time cai a média dele na série A acaba caindo (foi intencional)


            if (mediaGolsTime <= 0.3) distribuicaoMedia.merge("0,0-0,3", 1.0, Double::sum); 
            else if (mediaGolsTime <= 0.6) distribuicaoMedia.merge("0,3-0,6", 1.0, Double::sum);
            else if (mediaGolsTime <= 0.9) distribuicaoMedia.merge("0,6-0,9", 1.0, Double::sum);
            else if (mediaGolsTime <= 1.2) distribuicaoMedia.merge("0,9-1,2", 1.0, Double::sum);
            else distribuicaoMedia.merge("1,2+", 1.0, Double::sum);
        }
        return distribuicaoMedia;
    }

    public Time getEstatisticasTime(String nomeTime) {
        return estatisticasTimes.get(nomeTime);
    }


    public Map<String, Long> getEstadiosComMaisGols(List<String> regioesSelecionadas) {
        return partidas.stream()
            .filter(p -> regioesSelecionadas.isEmpty() || regioesSelecionadas.contains(getRegiaoDoTime(p.getTimeCasa())) || regioesSelecionadas.contains(getRegiaoDoTime(p.getTimeFora())))
            .collect(Collectors.groupingBy(
                Partida::getEstadio,
                Collectors.summingLong(p -> p.getGolsCasa() + p.getGolsFora())
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    public Map<String, Double> getPercentualInvencibilidade(List<String> regioesSelecionadas) {
        return estatisticasTimes.values().stream()
            .filter(time -> time.getJogos() > 0)
            .filter(time -> regioesSelecionadas.isEmpty() || regioesSelecionadas.contains(getRegiaoDoTime(time.getNome())))
            .collect(Collectors.toMap(
                Time::getNome,
                time -> ((double) (time.getVitorias() + time.getEmpates()) / time.getJogos()) * 100
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    public Map<String, int[]> getHistorico(List<String> regioesSelecionadas) {
        Map<String, int[]> historico = new HashMap<>(); // [títulos, top 4] havia pensdo em usar isso depois para outro gráfico de títulos

        Map<Integer, List<Partida>> partidasPorAno = partidas.stream()
                .collect(Collectors.groupingBy(Partida::getAno));

        for (Map.Entry<Integer, List<Partida>> entry : partidasPorAno.entrySet()) {
            List<Partida> partidasDoAno = entry.getValue();
            AnalisadorCampeonato analisadorDoAno = new AnalisadorCampeonato(partidasDoAno);
            List<Time> classificacaoAnual = analisadorDoAno.getTabelaClassificacao(false, regioesSelecionadas);

            if (classificacaoAnual.isEmpty()) continue;

            Time campeao = classificacaoAnual.get(0);
            historico.computeIfAbsent(campeao.getNome(), k -> new int[2])[0]++;

            int totalTimes = classificacaoAnual.size();
            if (totalTimes >= 20) {
                for (int i = 0; i < 4; i++) {
                    Time top = classificacaoAnual.get(totalTimes - 1 - i);
                    historico.computeIfAbsent(top.getNome(), k -> new int[2])[1]++;
                }
            }
        }
        return historico;
    }

    public static List<Partida> filtrarPorRegioes(List<Partida> partidas, List<String> regioes) {
        if (regioes == null || regioes.isEmpty()) {
            return partidas;
        }
        return partidas.stream()
            .filter(p -> {
                String regiaoCasa = TIME_REGIAO_MAP.getOrDefault(p.getTimeCasa(), "Desconhecida");
                String regiaoFora = TIME_REGIAO_MAP.getOrDefault(p.getTimeFora(), "Desconhecida");
                return regioes.contains(regiaoCasa) || regioes.contains(regiaoFora);
            })
            .collect(Collectors.toList());
    }
}