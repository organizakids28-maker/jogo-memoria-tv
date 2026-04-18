package com.example.jogomemoria;

  import android.app.Activity;
  import android.app.AlertDialog;
  import android.content.DialogInterface;
  import android.os.Bundle;
  import android.os.Handler;
  import android.view.KeyEvent;
  import android.view.View;
  import android.widget.Button;
  import android.widget.TableLayout;
  import android.widget.TableRow;
  import android.widget.TextView;

  import java.util.ArrayList;
  import java.util.Collections;
  import java.util.List;

  /**
   * MemoriaActivity — Jogo da Memória para Android TV.
   *
   * Usa TableLayout (compatível com todas versoes Android) em vez de GridLayout.
   * Navegacao pelo controle remoto: setas movem o foco, OK vira a carta.
   */
  public class MemoriaActivity extends Activity {

      private static final int TOTAL_CARTAS = 16;
      private static final int COLUNAS = 4;
      private static final int DELAY_FECHAR = 1000;

      private List<Carta> listaCartas;
      private Button[] botoes;

      private int indicePrimeira = -1;
      private int indiceSegunda = -1;
      private int paresEncontrados = 0;
      private boolean bloqueado = false;
      private int indiceFocado = 0;

      private Handler handler;
      private TextView tvPlacar;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_memoria);

          handler = new Handler();
          tvPlacar = (TextView) findViewById(R.id.tv_placar);

          inicializarCartas();
          criarBotoes();
          atualizarPlacar();

          // Foco inicial na primeira carta
          botoes[0].requestFocus();
      }

      /** Cria 8 pares (valores 1-8) e embaralha */
      private void inicializarCartas() {
          listaCartas = new ArrayList<Carta>();
          for (int valor = 1; valor <= 8; valor++) {
              listaCartas.add(new Carta(valor));
              listaCartas.add(new Carta(valor));
          }
          Collections.shuffle(listaCartas);
      }

      /**
       * Cria os 16 botoes em um TableLayout 4x4.
       * TableRow = linha, cada linha tem 4 botoes com layout_weight=1.
       * Compativel com todas as versoes do Android.
       */
      private void criarBotoes() {
          TableLayout table = (TableLayout) findViewById(R.id.table_cartas);
          botoes = new Button[TOTAL_CARTAS];

          for (int linha = 0; linha < 4; linha++) {
              // Cada linha do grid e um TableRow
              TableRow row = new TableRow(this);
              TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                  TableLayout.LayoutParams.MATCH_PARENT,
                  0,
                  1f  // peso igual para cada linha
              );
              row.setLayoutParams(rowParams);

              for (int col = 0; col < COLUNAS; col++) {
                  final int indice = linha * COLUNAS + col;

                  Button botao = new Button(this);
                  botao.setText("?");
                  botao.setTextSize(28);
                  botao.setTextColor(0xFFFFFFFF);
                  botao.setBackgroundResource(R.drawable.btn_selector);
                  botao.setFocusable(true);
                  botao.setFocusableInTouchMode(true);
                  botao.setTag(indice);

                  // Cada botao ocupa espaco igual na linha
                  TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                      0,
                      TableRow.LayoutParams.MATCH_PARENT,
                      1f
                  );
                  cellParams.setMargins(6, 6, 6, 6);
                  botao.setLayoutParams(cellParams);

                  botao.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          indiceFocado = indice;
                          processarClique(indice);
                      }
                  });

                  botao.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                      @Override
                      public void onFocusChange(View v, boolean hasFocus) {
                          if (hasFocus) indiceFocado = indice;
                      }
                  });

                  row.addView(botao);
                  botoes[indice] = botao;
              }

              table.addView(row);
          }
      }

      /** Navegacao pelo controle remoto da TV (D-pad) */
      @Override
      public boolean onKeyDown(int keyCode, KeyEvent event) {
          int novo = indiceFocado;

          switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_RIGHT:
                  if ((indiceFocado % COLUNAS) < COLUNAS - 1) novo = indiceFocado + 1;
                  break;
              case KeyEvent.KEYCODE_DPAD_LEFT:
                  if ((indiceFocado % COLUNAS) > 0) novo = indiceFocado - 1;
                  break;
              case KeyEvent.KEYCODE_DPAD_DOWN:
                  if (indiceFocado + COLUNAS < TOTAL_CARTAS) novo = indiceFocado + COLUNAS;
                  break;
              case KeyEvent.KEYCODE_DPAD_UP:
                  if (indiceFocado - COLUNAS >= 0) novo = indiceFocado - COLUNAS;
                  break;
              case KeyEvent.KEYCODE_DPAD_CENTER:
              case KeyEvent.KEYCODE_ENTER:
                  processarClique(indiceFocado);
                  return true;
              default:
                  return super.onKeyDown(keyCode, event);
          }

          if (novo != indiceFocado) {
              indiceFocado = novo;
              botoes[indiceFocado].requestFocus();
          }
          return true;
      }

      private void processarClique(int indice) {
          if (bloqueado) return;

          Carta carta = listaCartas.get(indice);
          if (carta.isEncontrada() || carta.isVirada()) return;

          carta.setVirada(true);
          botoes[indice].setText(String.valueOf(carta.getValor()));

          if (indicePrimeira == -1) {
              indicePrimeira = indice;
          } else {
              indiceSegunda = indice;
              bloqueado = true;
              verificarPar();
          }
      }

      private void verificarPar() {
          final Carta c1 = listaCartas.get(indicePrimeira);
          final Carta c2 = listaCartas.get(indiceSegunda);

          if (c1.getValor() == c2.getValor()) {
              c1.setEncontrada(true);
              c2.setEncontrada(true);
              paresEncontrados++;
              atualizarPlacar();
              botoes[indicePrimeira].setEnabled(false);
              botoes[indiceSegunda].setEnabled(false);
              resetarSelecao();

              if (paresEncontrados == TOTAL_CARTAS / 2) mostrarVitoria();
          } else {
              final int i1 = indicePrimeira;
              final int i2 = indiceSegunda;
              resetarSelecao();

              handler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      c1.setVirada(false);
                      c2.setVirada(false);
                      botoes[i1].setText("?");
                      botoes[i2].setText("?");
                      bloqueado = false;
                  }
              }, DELAY_FECHAR);
          }
      }

      private void resetarSelecao() {
          indicePrimeira = -1;
          indiceSegunda = -1;
          bloqueado = false;
      }

      private void atualizarPlacar() {
          tvPlacar.setText("Pares: " + paresEncontrados + " / 8");
      }

      private void mostrarVitoria() {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Parabens! Voce venceu!");
          builder.setMessage("Todos os 8 pares encontrados!");
          builder.setCancelable(false);
          builder.setPositiveButton("Jogar Novamente", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface d, int w) { reiniciarJogo(); }
          });
          builder.setNegativeButton("Sair", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface d, int w) { finish(); }
          });
          builder.show();
      }

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
          botoes[0].requestFocus();
      }
  }
  