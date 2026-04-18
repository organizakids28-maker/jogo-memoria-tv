package com.example.jogomemoria;

  import android.app.Activity;
  import android.content.Intent;
  import android.graphics.Color;
  import android.graphics.Typeface;
  import android.os.Bundle;
  import android.view.Gravity;
  import android.view.KeyEvent;
  import android.view.View;
  import android.widget.LinearLayout;
  import android.widget.TextView;

  public class MainActivity extends Activity {

      private static final int NUM_JOGOS = 2;
      private int focoAtual = 0;
      private TextView[] botoes = new TextView[NUM_JOGOS];

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          // Layout raiz
          LinearLayout raiz = new LinearLayout(this);
          raiz.setOrientation(LinearLayout.VERTICAL);
          raiz.setGravity(Gravity.CENTER);
          raiz.setBackgroundColor(Color.parseColor("#12103A"));
          raiz.setPadding(60, 40, 60, 40);

          // Título principal
          TextView titulo = new TextView(this);
          titulo.setText("🎮 Jogos para TV");
          titulo.setTextColor(Color.WHITE);
          titulo.setTextSize(32);
          titulo.setTypeface(Typeface.DEFAULT_BOLD);
          titulo.setGravity(Gravity.CENTER);
          LinearLayout.LayoutParams tituloLp = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          tituloLp.setMargins(0, 0, 0, 48);
          titulo.setLayoutParams(tituloLp);
          raiz.addView(titulo);

          // Subtítulo
          TextView sub = new TextView(this);
          sub.setText("Use o controle remoto para navegar • OK para selecionar");
          sub.setTextColor(Color.parseColor("#7070AA"));
          sub.setTextSize(14);
          sub.setGravity(Gravity.CENTER);
          LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          subLp.setMargins(0, -36, 0, 48);
          sub.setLayoutParams(subLp);
          raiz.addView(sub);

          // ─── Botão: Jogo da Memória ────────────────────────────────────────
          botoes[0] = criarBotaoJogo(
              "🃏  Jogo da Memória",
              "Encontre os 8 pares de cartas"
          );
          botoes[0].setOnClickListener(v -> abrirJogo(0));
          botoes[0].setOnFocusChangeListener((v, has) -> { if (has) focoAtual = 0; atualizarFoco(); });
          raiz.addView(botoes[0]);

          // Espaço entre botões
          View espaco = new View(this);
          espaco.setLayoutParams(new LinearLayout.LayoutParams(0, 20));
          raiz.addView(espaco);

          // ─── Botão: Complete a Palavra ─────────────────────────────────────
          botoes[1] = criarBotaoJogo(
              "✏️  Complete a Palavra",
              "Complete 26 palavras escolhendo a letra certa"
          );
          botoes[1].setOnClickListener(v -> abrirJogo(1));
          botoes[1].setOnFocusChangeListener((v, has) -> { if (has) focoAtual = 1; atualizarFoco(); });
          raiz.addView(botoes[1]);

          setContentView(raiz);
          botoes[0].requestFocus();
          atualizarFoco();
      }

      private TextView criarBotaoJogo(String nome, String descricao) {
          LinearLayout card = new LinearLayout(this);
          // Usamos TextView customizado como card clicável
          TextView tv = new TextView(this);
          tv.setText(nome + "\n" + descricao);
          tv.setTextColor(Color.WHITE);
          tv.setTextSize(20);
          tv.setTypeface(Typeface.DEFAULT_BOLD);
          tv.setGravity(Gravity.CENTER);
          tv.setFocusable(true);
          tv.setClickable(true);
          tv.setPadding(48, 36, 48, 36);
          tv.setLineSpacing(8, 1);

          LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          lp.setMargins(0, 0, 0, 0);
          tv.setLayoutParams(lp);

          return tv;
      }

      private void atualizarFoco() {
          for (int i = 0; i < NUM_JOGOS; i++) {
              if (i == focoAtual) {
                  botoes[i].setBackgroundColor(Color.parseColor("#4A2FBB"));
                  botoes[i].setTextColor(Color.parseColor("#FFD700"));
              } else {
                  botoes[i].setBackgroundColor(Color.parseColor("#22205A"));
                  botoes[i].setTextColor(Color.parseColor("#C0C0E0"));
              }
          }
      }

      private void abrirJogo(int idx) {
          Intent intent;
          if (idx == 0) {
              intent = new Intent(this, MemoriaActivity.class);
          } else {
              intent = new Intent(this, CompletaPalavraActivity.class);
          }
          startActivity(intent);
      }

      @Override
      public boolean onKeyDown(int keyCode, KeyEvent event) {
          switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_UP:
                  focoAtual = (focoAtual - 1 + NUM_JOGOS) % NUM_JOGOS;
                  botoes[focoAtual].requestFocus();
                  return true;
              case KeyEvent.KEYCODE_DPAD_DOWN:
                  focoAtual = (focoAtual + 1) % NUM_JOGOS;
                  botoes[focoAtual].requestFocus();
                  return true;
              case KeyEvent.KEYCODE_DPAD_CENTER:
              case KeyEvent.KEYCODE_ENTER:
                  abrirJogo(focoAtual);
                  return true;
          }
          return super.onKeyDown(keyCode, event);
      }
  }
  