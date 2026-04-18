package com.example.jogomemoria;

  import android.app.Activity;
  import android.app.AlertDialog;
  import android.content.DialogInterface;
  import android.os.Bundle;
  import android.os.Handler;
  import android.view.KeyEvent;
  import android.view.View;
  import android.view.animation.Animation;
  import android.view.animation.ScaleAnimation;
  import android.widget.ImageButton;
  import android.widget.TableLayout;
  import android.widget.TableRow;
  import android.widget.TextView;

  import java.util.ArrayList;
  import java.util.Collections;
  import java.util.List;

  /**
   * MemoriaActivity — Jogo da Memoria para Android TV.
   *
   * Cartas com IMAGENS e animacao de virada (flip).
   * Usa TableLayout (compativel com todas as versoes Android).
   * Navegacao pelo controle remoto: setas = mover, OK = virar carta.
   */
  public class MemoriaActivity extends Activity {

      private static final int TOTAL_CARTAS = 16;
      private static final int COLUNAS = 4;
      private static final int DELAY_FECHAR = 1200;
      private static final int DURACAO_ANIMACAO = 180; // ms de cada metade do flip

      // Imagens de cada par (indices 0-7 correspondem aos valores 1-8)
      private static final int[] IMAGENS_CARTA = {
          R.drawable.card_gato,
          R.drawable.card_cachorro,
          R.drawable.card_estrela,
          R.drawable.card_coracao,
          R.drawable.card_sol,
          R.drawable.card_lua,
          R.drawable.card_arco,
          R.drawable.card_raio
      };

      // Imagem do verso da carta (exibida quando esta fechada)
      private static final int IMAGEM_VERSO = R.drawable.card_verso;

      private List<Carta> listaCartas;
      private ImageButton[] botoes;

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

      /** Cria os 16 ImageButtons no TableLayout 4x4 */
      private void criarBotoes() {
          TableLayout table = (TableLayout) findViewById(R.id.table_cartas);
          botoes = new ImageButton[TOTAL_CARTAS];

          for (int linha = 0; linha < 4; linha++) {
              TableRow row = new TableRow(this);
              TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                  TableLayout.LayoutParams.MATCH_PARENT, 0, 1f);
              row.setLayoutParams(rowParams);

              for (int col = 0; col < COLUNAS; col++) {
                  final int indice = linha * COLUNAS + col;

                  ImageButton botao = new ImageButton(this);
                  // Exibe o verso da carta inicialmente
                  botao.setImageResource(IMAGEM_VERSO);
                  botao.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                  botao.setPadding(8, 8, 8, 8);
                  botao.setBackgroundResource(R.drawable.btn_selector);
                  botao.setFocusable(true);
                  botao.setFocusableInTouchMode(true);
                  botao.setTag(indice);

                  TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                      0, TableRow.LayoutParams.MATCH_PARENT, 1f);
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

      /**
       * Animacao de virada (flip) da carta.
       *
       * Funciona em duas etapas:
       * 1. Escala X de 1 -> 0 (carta "fecha" horizontalmente — 180ms)
       * 2. Troca a imagem no momento em que a escala e 0 (invisivel)
       * 3. Escala X de 0 -> 1 (carta "abre" com a nova imagem — 180ms)
       *
       * @param botao      O ImageButton a animar
       * @param novaImagem O resource ID da imagem a exibir apos a virada
       * @param aoTerminar Callback executado quando a animacao completa (pode ser null)
       */
      private void animarVirada(final ImageButton botao, final int novaImagem, final Runnable aoTerminar) {
          // --- Metade 1: encolhe horizontalmente (1 -> 0) ---
          ScaleAnimation sair = new ScaleAnimation(
              1f, 0f,   // escala X: de 1 a 0
              1f, 1f,   // escala Y: sem mudanca
              Animation.RELATIVE_TO_SELF, 0.5f,   // pivo X: centro
              Animation.RELATIVE_TO_SELF, 0.5f);  // pivo Y: centro
          sair.setDuration(DURACAO_ANIMACAO);
          sair.setFillAfter(false);

          sair.setAnimationListener(new Animation.AnimationListener() {
              @Override public void onAnimationStart(Animation a) {}
              @Override public void onAnimationRepeat(Animation a) {}

              @Override
              public void onAnimationEnd(Animation a) {
                  // Troca a imagem no momento em que a carta esta "invisivel"
                  botao.setImageResource(novaImagem);

                  // --- Metade 2: expande horizontalmente (0 -> 1) ---
                  ScaleAnimation entrar = new ScaleAnimation(
                      0f, 1f, 1f, 1f,
                      Animation.RELATIVE_TO_SELF, 0.5f,
                      Animation.RELATIVE_TO_SELF, 0.5f);
                  entrar.setDuration(DURACAO_ANIMACAO);
                  entrar.setFillAfter(false);

                  entrar.setAnimationListener(new Animation.AnimationListener() {
                      @Override public void onAnimationStart(Animation a) {}
                      @Override public void onAnimationRepeat(Animation a) {}
                      @Override
                      public void onAnimationEnd(Animation a) {
                          if (aoTerminar != null) aoTerminar.run();
                      }
                  });

                  botao.startAnimation(entrar);
              }
          });

          botao.startAnimation(sair);
      }

      /** Navegacao por controle remoto (D-pad) */
      @Override
      public boolean onKeyDown(int keyCode, KeyEvent event) {
          int novo = indiceFocado;
          switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_RIGHT:
                  if ((indiceFocado % COLUNAS) < COLUNAS - 1) novo = indiceFocado + 1; break;
              case KeyEvent.KEYCODE_DPAD_LEFT:
                  if ((indiceFocado % COLUNAS) > 0) novo = indiceFocado - 1; break;
              case KeyEvent.KEYCODE_DPAD_DOWN:
                  if (indiceFocado + COLUNAS < TOTAL_CARTAS) novo = indiceFocado + COLUNAS; break;
              case KeyEvent.KEYCODE_DPAD_UP:
                  if (indiceFocado - COLUNAS >= 0) novo = indiceFocado - COLUNAS; break;
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

      /** Processa o clique/selecao em uma carta */
      private void processarClique(int indice) {
          if (bloqueado) return;
          Carta carta = listaCartas.get(indice);
          if (carta.isEncontrada() || carta.isVirada()) return;

          carta.setVirada(true);

          // Anima a virada mostrando a imagem da frente
          final int imagemFrente = IMAGENS_CARTA[carta.getValor() - 1];
          animarVirada(botoes[indice], imagemFrente, null);

          if (indicePrimeira == -1) {
              indicePrimeira = indice;
          } else {
              indiceSegunda = indice;
              bloqueado = true;
              // Aguarda a animacao terminar antes de verificar o par
              handler.postDelayed(new Runnable() {
                  @Override public void run() { verificarPar(); }
              }, DURACAO_ANIMACAO * 2 + 100);
          }
      }

      /** Verifica se as duas cartas formam par */
      private void verificarPar() {
          final Carta c1 = listaCartas.get(indicePrimeira);
          final Carta c2 = listaCartas.get(indiceSegunda);

          if (c1.getValor() == c2.getValor()) {
              // Par encontrado!
              c1.setEncontrada(true);
              c2.setEncontrada(true);
              paresEncontrados++;
              atualizarPlacar();
              botoes[indicePrimeira].setEnabled(false);
              botoes[indiceSegunda].setEnabled(false);
              resetarSelecao();
              if (paresEncontrados == TOTAL_CARTAS / 2) mostrarVitoria();
          } else {
              // Par errado: fecha as cartas apos delay com animacao
              final int i1 = indicePrimeira;
              final int i2 = indiceSegunda;
              resetarSelecao();

              handler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      c1.setVirada(false);
                      c2.setVirada(false);
                      // Anima as duas cartas voltando para o verso
                      animarVirada(botoes[i1], IMAGEM_VERSO, null);
                      animarVirada(botoes[i2], IMAGEM_VERSO, new Runnable() {
                          @Override public void run() { bloqueado = false; }
                      });
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
          new AlertDialog.Builder(this)
              .setTitle("Parabens! Voce venceu!")
              .setMessage("Todos os 8 pares encontrados!")
              .setCancelable(false)
              .setPositiveButton("Jogar Novamente", new DialogInterface.OnClickListener() {
                  @Override public void onClick(DialogInterface d, int w) { reiniciarJogo(); }
              })
              .setNegativeButton("Sair", new DialogInterface.OnClickListener() {
                  @Override public void onClick(DialogInterface d, int w) { finish(); }
              })
              .show();
      }

      private void reiniciarJogo() {
          paresEncontrados = 0;
          indicePrimeira = -1;
          indiceSegunda = -1;
          bloqueado = false;
          indiceFocado = 0;
          inicializarCartas();
          for (int i = 0; i < TOTAL_CARTAS; i++) {
              botoes[i].setImageResource(IMAGEM_VERSO);
              botoes[i].setEnabled(true);
          }
          atualizarPlacar();
          botoes[0].requestFocus();
      }
  }
  