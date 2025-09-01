package com.brasileirao.dashboard.modelo;

public class Partida {
    private final int ano;
    private final int rodada;
    private final String data; // como String para facilitar 
    private final String timeCasa;
    private final String timeFora;
    private final int golsCasa;
    private final int golsFora;
    private final String estadio;

    public Partida(int ano, int rodada, String data, String timeCasa, String timeFora, int golsCasa, int golsFora, String estadio) {
        this.ano = ano;
        this.rodada = rodada;
        this.data = data;
        this.timeCasa = timeCasa;
        this.timeFora = timeFora;
        this.golsCasa = golsCasa;
        this.golsFora = golsFora;
        this.estadio = estadio;
    }

    // Getters para todos os campos
    public int getAno() { return ano; }
    public int getRodada() { return rodada; }
    public String getData() { return data; }
    public String getTimeCasa() { return timeCasa; }
    public String getTimeFora() { return timeFora; }
    public int getGolsCasa() { return golsCasa; }
    public int getGolsFora() { return golsFora; }
    public String getEstadio() { return estadio; }

    @Override
    public String toString() {
        return "Ano " + ano + " Rodada " + rodada + ": " + timeCasa + " " + golsCasa + " x " + golsFora + " " + timeFora;
    }
}