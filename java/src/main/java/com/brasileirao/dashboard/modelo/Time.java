
package com.brasileirao.dashboard.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Time {
    private final SimpleStringProperty nome;
    private final SimpleIntegerProperty pontos;
    private final SimpleIntegerProperty jogos;
    private final SimpleIntegerProperty vitorias;
    private final SimpleIntegerProperty empates;
    private final SimpleIntegerProperty derrotas;
    private final SimpleIntegerProperty golsPro;
    private final SimpleIntegerProperty golsContra;
    private final SimpleIntegerProperty saldoGols;

    public Time(String nome) {
        this.nome = new SimpleStringProperty(nome);
        this.pontos = new SimpleIntegerProperty(0);
        this.jogos = new SimpleIntegerProperty(0);
        this.vitorias = new SimpleIntegerProperty(0);
        this.empates = new SimpleIntegerProperty(0);
        this.derrotas = new SimpleIntegerProperty(0);
        this.golsPro = new SimpleIntegerProperty(0);
        this.golsContra = new SimpleIntegerProperty(0);
        this.saldoGols = new SimpleIntegerProperty(0);
    }

    // Getters para os valores 
    public String getNome() { return nome.get(); }
    public int getPontos() { return pontos.get(); }
    public int getJogos() { return jogos.get(); }
    public int getVitorias() { return vitorias.get(); }
    public int getEmpates() { return empates.get(); }
    public int getDerrotas() { return derrotas.get(); }
    public int getGolsMarc() { return golsPro.get(); }
    public int getGolsSofr() { return golsContra.get(); }
    public int getSaldoGols() { return saldoGols.get(); }

    // MÉTODOS DE PROPRIEDADE 
    // A TableView usa estes métodos para se conectar aos dados
    public SimpleStringProperty nomeProperty() { return nome; }
    public SimpleIntegerProperty pontosProperty() { return pontos; }
    public SimpleIntegerProperty jogosProperty() { return jogos; }
    public SimpleIntegerProperty vitoriasProperty() { return vitorias; }
    public SimpleIntegerProperty empatesProperty() { return empates; }
    public SimpleIntegerProperty derrotasProperty() { return derrotas; }
    public SimpleIntegerProperty golsProProperty() { return golsPro; }
    public SimpleIntegerProperty golsContraProperty() { return golsContra; }
    public SimpleIntegerProperty saldoGolsProperty() { return saldoGols; }

    // Métodos para atualizar as estatísticas
    public void adicionarVitoria(int golsPro, int golsContra) {
        this.jogos.set(getJogos() + 1);
        this.vitorias.set(getVitorias() + 1);
        this.pontos.set(getPontos() + 3);
        this.golsPro.set(getGolsMarc() + golsPro);
        this.golsContra.set(getGolsSofr() + golsContra);
        atualizarSaldoGols();
    }

    public void adicionarEmpate(int gols) {
        this.jogos.set(getJogos() + 1);
        this.empates.set(getEmpates() + 1);
        this.pontos.set(getPontos() + 1);
        this.golsPro.set(getGolsMarc() + gols);
        this.golsContra.set(getGolsSofr() + gols);
    }

    public void adicionarDerrota(int golsPro, int golsContra) {
        this.jogos.set(getJogos() + 1);
        this.derrotas.set(getDerrotas() + 1);
        this.golsPro.set(getGolsMarc() + golsPro);
        this.golsContra.set(getGolsSofr() + golsContra);
        atualizarSaldoGols();
    }

    private void atualizarSaldoGols() {
        this.saldoGols.set(getGolsMarc() - getGolsSofr());
    }
}