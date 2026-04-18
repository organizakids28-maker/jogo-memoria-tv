package com.example.jogomemoria;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MemoriaActivity — Jogo da Memória otimizado para Android TV.
 *
 * Navegação pelo controle remoto:
 * - Setas direcionais (cima/baixo/esquerda/direita): movem o foco entre as cartas
 * - Botão OK (ou Enter/Center): vira a carta selecionada
 * - Botão Voltar: sai do jogo
 *
 * Grid 4x4 com 16 cartas (8 pares).
 * Destaque visual (cor amarela) indica qual carta está selecionada.
 */
public class MemoriaActivity extends Activity {

    // Número total de cartas no grid (4 x 4 = 16)
    private static final int TOTAL_CARTAS = 16;

    // Número de colunas do grid
    private static final int COLUNAS = 4;

    // Número de linhas do grid
    private static final int LINHAS = 4;

    // Delay antes de fechar cartas erradas (milissegundos)
    private static final int DELAY_FECHAR = 1000;

    // Lista com todas as cartas e seus estados
    private List<Carta> listaCartas;

    // Array de botões (um por carta)
    private Button[] botoes;

    // Índice da primeira carta virada neste turno (-1 = nenhuma)
    private int indicePrimeira = -1;

    // Índice da segunda carta virada neste turno (-1 = nenhuma)
    private int indiceSegunda = -1;

    // Quantos pares foram encontrados até agora
    private int paresEncontrados = 0;

    // Bloqueia cliques durante o delay de fechamento
    private boolean bloqueado = false;

    // Handler para agendar o fechamento das cartas com delay
    private Handler handler;

    // Placar exibido no topo da tela
    private TextView tvPlacar;

    // Índice da carta atualmente com foco (navegação por controle remoto)
    // Começa na posição 0 (canto superior esquerdo do grid)
    private int indiceFocado = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memoria);

        handler = new Handler();
        tvPlacar = (TextView) findViewById(R.id.tv_placar);

        // Cria e embaralha as cartas
        inicializarCartas();

        // Cria os botões no grid
        criarBotoes();

        // Atualiza o placar inicial
        atualizarPlacar();

        // Coloca o foco visual na primeira carta
        botoes[indiceFocado].requestFocus();
    }

    /**
     * Cria 8 pares de cartas (valores 1–8, cada um duas vezes) e embaralha.
     */
    private void inicializarCartas() {
        listaCartas = new ArrayList<Carta>();
        for (int valor = 1; valor <= 8; valor++) {
            listaCartas.add(new Carta(valor));
            listaCartas.add(new Carta(valor));
        }
        Collections.shuffle(listaCartas);
    }

    /**
     * Cria os 16 botões dinamicamente e adiciona ao GridLayout.
     * Cada botão é configurado para responder ao foco do controle remoto.
     */
    private void criarBotoes() {
        GridLayout gridLayout = (GridLayout) findViewById(R.id.grid_cartas);
        botoes = new Button[TOTAL_CARTAS];

        for (int i = 0; i < TOTAL_CARTAS; i++) {
            Button botao = new Button(this);

            // Carta começa fechada
            botao.setText("?");
            botao.setTextSize(28);
            botao.setTextColor(0xFFFFFFFF);

            // Usa o seletor de cores que muda conforme foco/estado
            botao.setBackgroundResource(R.drawable.btn_selector);

            // IMPORTANTE para TV: o botão precisa ser focável pelo D-pad
            botao.setFocusable(true);
            botao.setFocusableInTouchMode(true);

            // Tag guarda o índice da carta
            final int indice = i;
            botao.setTag(indice);

            // Parâmetros do GridLayout: cada botão ocupa 1 célula
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(i % COLUNAS, 1f);
            params.rowSpec = GridLayout.spec(i / COLUNAS, 1f);
            params.setMargins(8, 8, 8, 8);
            botao.setLayoutParams(params);

            // Listener de clique (funciona com toque E com OK do controle remoto)
            botao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    indiceFocado = indice;
                    processarClique(indice);
                }
            });

            // Quando o botão recebe foco (controle remoto passou por ele),
            // registra qual é o índice atual com foco
            botao.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        indiceFocado = indice;
                    }
                }
            });

            gridLayout.addView(botao);
            botoes[i] = botao;
        }
    }

    /**
     * Intercepta teclas do controle remoto para navegação manual no grid.
     *
     * O GridLayout já suporta navegação básica pelo D-pad automaticamente,
     * mas este método garante que as bordas do grid "enrolem" (wrap-around),
     * ou seja: pressionar direita na última coluna vai para a primeira da próxima linha.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int novoIndice = indiceFocado;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // Move para direita; se na última coluna, vai para início da próxima linha
                if ((indiceFocado % COLUNAS) < COLUNAS - 1) {
                    novoIndice = indiceFocado + 1;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                // Move para esquerda; se na primeira coluna, vai para fim da linha anterior
                if ((indiceFocado % COLUNAS) > 0) {
                    novoIndice = indiceFocado - 1;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Move para baixo; se na última linha, fica no lugar
                if (indiceFocado + COLUNAS < TOTAL_CARTAS) {
                    novoIndice = indiceFocado + COLUNAS;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                // Move para cima; se na primeira linha, fica no lugar
                if (indiceFocado - COLUNAS >= 0) {
                    novoIndice = indiceFocado - COLUNAS;
                }
                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // Botão OK ou Enter: vira a carta com foco atual
                processarClique(indiceFocado);
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }

        // Move o foco visual para o novo botão
        if (novoIndice != indiceFocado) {
            indiceFocado = novoIndice;
            botoes[indiceFocado].requestFocus();
        }

        return true;
    }

    /**
     * Processa o clique/seleção em uma carta.
     * Controla a lógica de virar e verificar pares.
     *
     * @param indice Índice da carta selecionada (0–15)
     */
    private void processarClique(int indice) {
        // Ignora se o jogo está bloqueado aguardando o delay
        if (bloqueado) return;

        Carta carta = listaCartas.get(indice);

        // Ignora cartas já encontradas ou já viradas neste turno
        if (carta.isEncontrada() || carta.isVirada()) return;

        // Vira a carta: mostra o valor
        carta.setVirada(true);
        botoes[indice].setText(String.valueOf(carta.getValor()));

        if (indicePrimeira == -1) {
            // Primeira carta deste turno
            indicePrimeira = indice;
        } else {
            // Segunda carta deste turno
            indiceSegunda = indice;
            bloqueado = true;
            verificarPar();
        }
    }

    /**
     * Verifica se as duas cartas viradas formam um par.
     * - Igual: marca como encontradas, atualiza placar, verifica vitória.
     * - Diferente: agenda fechamento após delay.
     */
    private void verificarPar() {
        final Carta carta1 = listaCartas.get(indicePrimeira);
        final Carta carta2 = listaCartas.get(indiceSegunda);

        if (carta1.getValor() == carta2.getValor()) {
            // ---- Par encontrado! ----
            carta1.setEncontrada(true);
            carta2.setEncontrada(true);
            paresEncontrados++;
            atualizarPlacar();

            // Deixa os botões encontrados verdes e desabilitados
            botoes[indicePrimeira].setEnabled(false);
            botoes[indiceSegunda].setEnabled(false);

            resetarSelecao();

            // Verifica vitória (todos os 8 pares encontrados)
            if (paresEncontrados == TOTAL_CARTAS / 2) {
                mostrarMensagemVitoria();
            }

        } else {
            // ---- Par errado: fecha após delay ----
            final int idx1 = indicePrimeira;
            final int idx2 = indiceSegunda;

            resetarSelecao();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    carta1.setVirada(false);
                    carta2.setVirada(false);
                    botoes[idx1].setText("?");
                    botoes[idx2].setText("?");
                    bloqueado = false;
                }
            }, DELAY_FECHAR);
        }
    }

    /**
     * Reseta os índices de seleção do turno atual.
     */
    private void resetarSelecao() {
        indicePrimeira = -1;
        indiceSegunda = -1;
        bloqueado = false;
    }

    /**
     * Atualiza o texto do placar na tela.
     */
    private void atualizarPlacar() {
        tvPlacar.setText("Pares: " + paresEncontrados + " / 8");
    }

    /**
     * Exibe o diálogo de vitória com opção de jogar novamente ou sair.
     * Usa AlertDialog padrão, compatível com TV (navegável pelo controle).
     */
    private void mostrarMensagemVitoria() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Parabéns! Você venceu!");
        builder.setMessage("Todos os 8 pares foram encontrados!\nDeseja jogar novamente?");
        builder.setCancelable(false);

        builder.setPositiveButton("Jogar Novamente", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reiniciarJogo();
            }
        });

        builder.setNegativeButton("Sair", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.show();
    }

    /**
     * Reinicia o jogo: novo embaralhamento, reseta todos os estados.
     */
    private void reiniciarJogo() {
        paresEncontrados = 0;
        indicePrimeira = -1;
        indiceSegunda = -1;
        bloqueado = false;
        indiceFocado = 0;

        inicializarCartas();

        for (int i = 0; i < TOTAL_CARTAS; i++) {
            botoes[i].setText("?");
            botoes[i].setEnabled(true);
        }

        atualizarPlacar();

        // Devolve o foco para a primeira carta
        botoes[0].requestFocus();
    }
}
