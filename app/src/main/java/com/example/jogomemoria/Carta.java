package com.example.jogomemoria;

/**
 * Classe que representa uma carta do Jogo da Memória.
 *
 * Cada carta tem:
 * - um valor (número que identifica o par)
 * - um estado "virada" (se está com o valor visível ou não)
 * - um estado "encontrada" (se o par já foi encontrado e deve ficar aberta)
 */
public class Carta {

    // Valor da carta (1 a 8), cartas com mesmo valor formam um par
    private int valor;

    // Indica se a carta está virada para cima (valor visível)
    private boolean virada;

    // Indica se a carta já foi encontrada (par completo)
    private boolean encontrada;

    /**
     * Construtor: cria uma carta com o valor informado.
     * Por padrão, a carta começa fechada (não virada) e não encontrada.
     *
     * @param valor O valor numérico da carta (1–8)
     */
    public Carta(int valor) {
        this.valor = valor;
        this.virada = false;
        this.encontrada = false;
    }

    // ---- Getters e Setters ----

    /** Retorna o valor da carta */
    public int getValor() {
        return valor;
    }

    /** Retorna true se a carta está virada para cima */
    public boolean isVirada() {
        return virada;
    }

    /** Define se a carta está virada ou não */
    public void setVirada(boolean virada) {
        this.virada = virada;
    }

    /** Retorna true se o par desta carta já foi encontrado */
    public boolean isEncontrada() {
        return encontrada;
    }

    /** Define se a carta foi encontrada (par completo) */
    public void setEncontrada(boolean encontrada) {
        this.encontrada = encontrada;
    }
}
