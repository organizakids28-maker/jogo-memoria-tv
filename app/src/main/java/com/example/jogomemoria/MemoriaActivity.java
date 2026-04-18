package com.example.jogomemoria;

  import android.app.Activity;
  import android.graphics.Color;
  import android.media.AudioManager;
  import android.media.ToneGenerator;
  import android.os.Bundle;
  import android.os.Handler;
  import android.view.Gravity;
  import android.view.KeyEvent;
  import android.view.View;
  import android.view.animation.Animation;
  import android.view.animation.AnimationUtils;
  import android.view.animation.ScaleAnimation;
  import android.widget.ImageButton;
  import android.widget.TableLayout;
  import android.widget.TableRow;
  import android.widget.TextView;

  import java.util.ArrayList;
  import java.util.Collections;

  public class MemoriaActivity extends Activity {

      private static final int LINHAS  = 4;
      private static final int COLUNAS = 4;
      private static final int TOTAL   = LINHAS * COLUNAS;

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

      private Carta[]       cartas        = new Carta[TOTAL];
      private ImageButton[] botoes        = new ImageButton[TOTAL];
      private int           primeiraSel   = -1;
      private boolean       bloqueado     = false;
      private int           paresEncontrados = 0;
      private int           focoAtual     = 0;
      private TextView      tvPares;
      private Handler       handler       = new Handler();
      private ToneGenerator toneGen;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_memoria);

          try {
              toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
          } catch (Exception e) {
              toneGen = null;
          }

          tvPares = findViewById(R.id.tv_pares);

          ArrayList<Integer> valores = new ArrayList<>();
          for (int i = 0; i < 8; i++) { valores.add(i); valores.add(i); }
          Collections.shuffle(valores);
          for (int i = 0; i < TOTAL; i++) cartas[i] = new Carta(valores.get(i));

          TableLayout tabela = findViewById(R.id.tabela);
          int indice = 0;
          for (int l = 0; l < LINHAS; l++) {
              TableRow linha = new TableRow(this);
              linha.setLayoutParams(new TableLayout.LayoutParams(
                  TableLayout.LayoutParams.MATCH_PARENT,
                  TableLayout.LayoutParams.WRAP_CONTENT, 1f));
              for (int c = 0; c < COLUNAS; c++) {
                  final int idx = indice++;
                  ImageButton btn = new ImageButton(this);
                  TableRow.LayoutParams lp = new TableRow.LayoutParams(0,
                      TableRow.LayoutParams.MATCH_PARENT, 1f);
                  lp.setMargins(6, 6, 6, 6);
                  btn.setLayoutParams(lp);
                  btn.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                  btn.setPadding(4, 4, 4, 4);
                  btn.setImageResource(R.drawable.card_verso);
                  btn.setBackgroundColor(Color.TRANSPARENT);
                  btn.setFocusable(true);
                  btn.setOnClickListener(v -> clicarCarta(idx));
                  btn.setOnFocusChangeListener((v, hasFocus) -> {
                      v.setBackgroundColor(hasFocus
                          ? Color.argb(180, 255, 215, 0)
                          : Color.TRANSPARENT);
                      if (hasFocus) focoAtual = idx;
                  });
                  botoes[idx] = btn;
                  linha.addView(btn);
              }
              tabela.addView(linha);
          }

          botoes[0].requestFocus();
      }

      private void tocarSom(final int tipo) {
          if (toneGen == null) return;
          handler.post(() -> {
              try {
                  switch (tipo) {
                      case 0: // virar carta: bip curto agudo
                          toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 60);
                          break;
                      case 1: // acerto: dois bips crescentes
                          toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 200);
                          break;
                      case 2: // erro: bip grave
                          toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 300);
                          break;
                      case 3: // vitória: bips de teclado
                          toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 600);
                          break;
                  }
              } catch (Exception ignored) {}
          });
      }

      private void clicarCarta(int idx) {
          if (bloqueado) return;
          Carta carta = cartas[idx];
          if (carta.encontrada || carta.virada) return;

          tocarSom(0);
          virarCartaAnimado(idx, true, () -> {
              carta.virada = true;

              if (primeiraSel == -1) {
                  primeiraSel = idx;
              } else {
                  int primeiro = primeiraSel;
                  primeiraSel = -1;
                  bloqueado = true;

                  if (cartas[primeiro].valor == carta.valor) {
                      // Par correto
                      handler.postDelayed(() -> {
                          tocarSom(1);
                          cartas[primeiro].encontrada = true;
                          cartas[idx].encontrada = true;
                          botoes[primeiro].setBackgroundColor(Color.argb(100, 0, 200, 0));
                          botoes[idx].setBackgroundColor(Color.argb(100, 0, 200, 0));
                          paresEncontrados++;
                          tvPares.setText("Pares: " + paresEncontrados + "/8");
                          bloqueado = false;
                          if (paresEncontrados == 8) {
                              handler.postDelayed(() -> tocarSom(3), 400);
                              handler.postDelayed(() -> mostrarVitoria(), 800);
                          }
                      }, 300);
                  } else {
                      // Par errado
                      handler.postDelayed(() -> {
                          tocarSom(2);
                          virarCartaAnimado(primeiro, false, () -> cartas[primeiro].virada = false);
                          virarCartaAnimado(idx, false, () -> {
                              carta.virada = false;
                              bloqueado = false;
                          });
                      }, 800);
                  }
              }
          });
      }

      private void virarCartaAnimado(int idx, boolean abrindo, Runnable aoConcluir) {
          ImageButton btn = botoes[idx];
          Carta carta = cartas[idx];

          ScaleAnimation fechar = new ScaleAnimation(
              1f, 0f, 1f, 1f,
              Animation.RELATIVE_TO_SELF, 0.5f,
              Animation.RELATIVE_TO_SELF, 0.5f);
          fechar.setDuration(180);

          ScaleAnimation abrir = new ScaleAnimation(
              0f, 1f, 1f, 1f,
              Animation.RELATIVE_TO_SELF, 0.5f,
              Animation.RELATIVE_TO_SELF, 0.5f);
          abrir.setDuration(180);

          fechar.setAnimationListener(new Animation.AnimationListener() {
              public void onAnimationStart(Animation a) {}
              public void onAnimationRepeat(Animation a) {}
              public void onAnimationEnd(Animation a) {
                  if (abrindo) {
                      btn.setImageResource(IMAGENS_CARTA[carta.valor]);
                  } else {
                      btn.setImageResource(R.drawable.card_verso);
                  }
                  btn.startAnimation(abrir);
              }
          });

          abrir.setAnimationListener(new Animation.AnimationListener() {
              public void onAnimationStart(Animation a) {}
              public void onAnimationRepeat(Animation a) {}
              public void onAnimationEnd(Animation a) {
                  if (aoConcluir != null) aoConcluir.run();
              }
          });

          btn.startAnimation(fechar);
      }

      private void mostrarVitoria() {
          android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
          b.setTitle("🏆 Parabéns!");
          b.setMessage("Você encontrou todos os 8 pares!");
          b.setPositiveButton("Jogar de novo", (d, w) -> reiniciarJogo());
          b.setNegativeButton("Sair", (d, w) -> finish());
          b.setCancelable(false);
          b.show();
      }

      private void reiniciarJogo() {
          paresEncontrados = 0;
          primeiraSel = -1;
          bloqueado = false;
          tvPares.setText("Pares: 0/8");

          ArrayList<Integer> valores = new ArrayList<>();
          for (int i = 0; i < 8; i++) { valores.add(i); valores.add(i); }
          Collections.shuffle(valores);
          for (int i = 0; i < TOTAL; i++) {
              cartas[i] = new Carta(valores.get(i));
              botoes[i].setImageResource(R.drawable.card_verso);
              botoes[i].setBackgroundColor(Color.TRANSPARENT);
          }
          botoes[0].requestFocus();
      }

      @Override
      public boolean onKeyDown(int keyCode, KeyEvent event) {
          switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_CENTER:
              case KeyEvent.KEYCODE_ENTER:
                  clicarCarta(focoAtual);
                  return true;
          }
          return super.onKeyDown(keyCode, event);
      }

      @Override
      protected void onDestroy() {
          super.onDestroy();
          if (toneGen != null) { toneGen.release(); toneGen = null; }
      }
  }
  