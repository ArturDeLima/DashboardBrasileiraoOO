package com.brasileirao.dashboard.dados;

import com.brasileirao.dashboard.modelo.Partida;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeitorCSV {

    @SuppressWarnings("CallToPrintStackTrace")
    public static List<Partida> carregarPartidas(String caminhoArquivo) {
        List<Partida> partidas = new ArrayList<>();
        String linha = "";

        int ano = extrairAnoDoNomeArquivo(caminhoArquivo);

        // ** CÓDIGO CORRIGIDO: Usa o ClassLoader para carregar o arquivo como um recurso **
        try (InputStream is = LeitorCSV.class.getClassLoader().getResourceAsStream(caminhoArquivo)) {
            if (is == null) {
                System.err.println("ERRO: O arquivo " + caminhoArquivo + " não foi encontrado no classpath.");
                return partidas;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                // 1. Ignorar a primeira linha - que é o cabecalho
                br.readLine();

                // 2. Ler o arquivo linha por linha a partir da segunda linha
                while ((linha = br.readLine()) != null) {
                    // Usa a vírgula como separador
                    String[] campos = linha.split(";");

                    // 3. Verifica se a linha tem o numero min de colunas esperado para evitar erros
                    if (campos.length >= 7) {
                        try {
                            // 4. Extrai e converte os dados
                            int rodada = Integer.parseInt(campos[0].trim());
                            String data = campos[1].trim();
                            String timeCasa = campos[3].trim();
                            String timeFora = campos[4].trim();
                            // Trata o caso de gols serem vazios ou nao-numericos
                            int golsCasa = campos[5].trim().isEmpty() ? 0 : Integer.parseInt(campos[5].trim());
                            int golsFora = campos[6].trim().isEmpty() ? 0 : Integer.parseInt(campos[6].trim());
                            String estadio = campos.length > 9 ? campos[9].trim() : "";

                            // 5. Cria o objeto Partida e adiciona a lista
                            partidas.add(new Partida(ano, rodada, data, timeCasa, timeFora, golsCasa, golsFora, estadio));
                        } catch (NumberFormatException e) {
                            System.err.println("AVISO: Linha com formato numerico invalido foi ignorada: " + linha);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("ERRO: Falha ao ler o arquivo CSV: " + e.getMessage());
            e.printStackTrace();
        }
        return partidas;
    }
    
    private static int extrairAnoDoNomeArquivo(String nomeArquivo) {
        Pattern pattern = Pattern.compile("(\\d{4})");
        Matcher matcher = pattern.matcher(nomeArquivo);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}