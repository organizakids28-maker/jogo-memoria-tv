package com.example.jogomemoria;

  import android.app.Activity;
  import android.app.AlertDialog;
  import android.graphics.Color;
  import android.media.AudioManager;
  import android.media.ToneGenerator;
  import android.os.Bundle;
  import android.os.Handler;
  import android.view.Gravity;
  import android.view.KeyEvent;
  import android.view.View;
  import android.widget.LinearLayout;
  import android.widget.ScrollView;
  import android.widget.TextView;

  import java.util.ArrayList;
  import java.util.Arrays;
  import java.util.Collections;
  import java.util.List;

  public class CompletaPalavraActivity extends Activity {

      // ─── Banco de palavras ─────────────────────────────────────────────────
      private static final String[][] PALAVRAS = {
          {"GATO",       "Animal",     "Mia e gosta de dormir"},
          {"CACHORRO",   "Animal",     "Melhor amigo do homem"},
          {"ELEFANTE",   "Animal",     "Maior animal terrestre"},
          {"BORBOLETA",  "Animal",     "Inseto colorido que voa"},
          {"JACARE",     "Animal",     "Reptil de dentes afiados"},
          {"TARTARUGA",  "Animal",     "Animal lento com casco"},
          {"BANANA",     "Fruta",      "Amarela e dos macacos"},
          {"ABACAXI",    "Fruta",      "Tropical com casca espinhosa"},
          {"MELANCIA",   "Fruta",      "Vermelha por dentro"},
          {"MORANGO",    "Fruta",      "Vermelho e pequeno"},
          {"BRASIL",     "Pais",       "Pais do futebol e carnaval"},
          {"PORTUGAL",   "Pais",       "Pais europeu que fala portugues"},
          {"ARGENTINA",  "Pais",       "Vizinho do Brasil ao sul"},
          {"FUTEBOL",    "Esporte",    "Esporte mais popular do Brasil"},
          {"NATACAO",    "Esporte",    "Praticado na piscina"},
          {"VOLEIBOL",   "Esporte",    "Rede com 6 jogadores"},
          {"CABECA",     "Corpo",      "Parte acima do pescoco"},
          {"COTOVELO",   "Corpo",      "Dobra do braco"},
          {"AMARELO",    "Cor",        "Cor do sol e da banana"},
          {"ROXO",       "Cor",        "Mistura de azul com vermelho"},
          {"PROFESSOR",  "Profissao",  "Ensina na escola"},
          {"MEDICO",     "Profissao",  "Cuida da saude das pessoas"},
          {"ESCOLA",     "Lugar",      "Lugar onde se aprende"},
          {"HOSPITAL",   "Lugar",      "Lugar para tratar doentes"},
          {"COMPUTADOR", "Tecnologia", "Maquina para trabalhar e jogar"},
          {"CELULAR",    "Tecnologia", "Telefone que cabe no bolso"},
      };

      private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      private static final int VIDAS_INICIAIS = 5;
      private static final int PONTOS_ACERTO  = 10;
      private static final int NUM_OPCOES     = 4;

      // ─── Estado do jogo ────────────────────────────────────────────────────
      private int        pontos           = 0;
      private int        vidas            = VIDAS_INICIAIS;
      private int        palavraIndex     = 0;
      private int        blankAtual       = 0;
      private String     palavraAtual     = "";
      private String     categoriaAtual   = "";
      private String     dicaAtual        = "";
      private int[]      blanks           = {};
      private char[]     reveladas        = {};
      private String[]   opcoes           = new String[NUM_OPCOES];
      private int        focoOpcao        = 0;
      private List<Integer> ordemPalavras;

      // ─── Views ─────────────────────────────────────────────────────────────
      private TextView    tvTitulo;
      private TextView    tvProgresso;
      private TextView    tvPontos;
      private TextView    tvVidas;
      private TextView    tvCategoria;
      private TextView    tvDica;
      private LinearLayout llLetras;
      private LinearLayout llOpcoes;
      private TextView    tvFeedback;
      private TextView[]  tvLetraViews;
      private TextView[]  tvOpcaoViews;

      private Handler      handler  = new Handler();
      private ToneGenerator toneGen;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          try { toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 80); }
          catch (Exception e) { toneGen = null; }

          ordemPalavras = new ArrayList<>();
          for (int i = 0; i < PALAVRAS.length; i++) ordemPalavras.add(i);
          Collections.shuffle(ordemPalavras);

          construirLayout();
          carregarPalavra();
      }

      private void construirLayout() {
          ScrollView scroll = new ScrollView(this);
          scroll.setBackgroundColor(Color.parseColor("#1A1040"));
          scroll.setFillViewport(true);

          LinearLayout root = new LinearLayout(this);
          root.setOrientation(LinearLayout.VERTICAL);
          root.setGravity(Gravity.CENTER_HORIZONTAL);
          root.setPadding(24, 24, 24, 24);
          scroll.addView(root);

          // Cabeçalho
          LinearLayout cabecalho = new LinearLayout(this);
          cabecalho.setOrientation(LinearLayout.HORIZONTAL);
          cabecalho.setLayoutParams(new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

          LinearLayout esq = new LinearLayout(this);
          esq.setOrientation(LinearLayout.VERTICAL);
          LinearLayout.LayoutParams wEsq = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
          esq.setLayoutParams(wEsq);

          tvProgresso = new TextView(this);
          tvProgresso.setTextColor(Color.parseColor("#A0C4FF"));
          tvProgresso.setTextSize(13);
          esq.addView(tvProgresso);

          tvTitulo = new TextView(this);
          tvTitulo.setText("Complete a Palavra");
          tvTitulo.setTextColor(Color.WHITE);
          tvTitulo.setTextSize(20);
          tvTitulo.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
          esq.addView(tvTitulo);
          cabecalho.addView(esq);

          LinearLayout dir = new LinearLayout(this);
          dir.setOrientation(LinearLayout.VERTICAL);
          dir.setGravity(Gravity.END);
          LinearLayout.LayoutParams wDir = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          dir.setLayoutParams(wDir);

          tvPontos = new TextView(this);
          tvPontos.setTextColor(Color.parseColor("#FFD700"));
          tvPontos.setTextSize(18);
          tvPontos.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
          tvPontos.setGravity(Gravity.END);
          dir.addView(tvPontos);

          tvVidas = new TextView(this);
          tvVidas.setTextColor(Color.parseColor("#FF6B6B"));
          tvVidas.setTextSize(18);
          tvVidas.setGravity(Gravity.END);
          dir.addView(tvVidas);
          cabecalho.addView(dir);
          root.addView(cabecalho);

          // Espaço
          View sep = new View(this);
          sep.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
          sep.setBackgroundColor(Color.parseColor("#3A3A7A"));
          LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
          sepLp.setMargins(0, 16, 0, 16);
          sep.setLayoutParams(sepLp);
          root.addView(sep);

          // Card de categoria e dica
          LinearLayout card = new LinearLayout(this);
          card.setOrientation(LinearLayout.VERTICAL);
          card.setGravity(Gravity.CENTER);
          card.setBackgroundColor(Color.parseColor("#22205A"));
          card.setPadding(24, 20, 24, 20);
          LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          cardLp.setMargins(0, 0, 0, 16);
          card.setLayoutParams(cardLp);

          tvCategoria = new TextView(this);
          tvCategoria.setGravity(Gravity.CENTER);
          tvCategoria.setTextColor(Color.parseColor("#A0C4FF"));
          tvCategoria.setTextSize(14);
          tvCategoria.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
          card.addView(tvCategoria);

          tvDica = new TextView(this);
          tvDica.setGravity(Gravity.CENTER);
          tvDica.setTextColor(Color.parseColor("#C0C0D0"));
          tvDica.setTextSize(15);
          tvDica.setPadding(0, 8, 0, 16);
          card.addView(tvDica);

          // Letras da palavra
          llLetras = new LinearLayout(this);
          llLetras.setOrientation(LinearLayout.HORIZONTAL);
          llLetras.setGravity(Gravity.CENTER);
          llLetras.setLayoutParams(new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
          card.addView(llLetras);

          tvFeedback = new TextView(this);
          tvFeedback.setGravity(Gravity.CENTER);
          tvFeedback.setTextSize(16);
          tvFeedback.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
          tvFeedback.setPadding(0, 12, 0, 0);
          tvFeedback.setVisibility(View.INVISIBLE);
          card.addView(tvFeedback);

          root.addView(card);

          // Opções de letras
          llOpcoes = new LinearLayout(this);
          llOpcoes.setOrientation(LinearLayout.HORIZONTAL);
          llOpcoes.setGravity(Gravity.CENTER);
          LinearLayout.LayoutParams opcoesLp = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          opcoesLp.setMargins(0, 8, 0, 0);
          llOpcoes.setLayoutParams(opcoesLp);

          tvOpcaoViews = new TextView[NUM_OPCOES];
          for (int i = 0; i < NUM_OPCOES; i++) {
              final int idx = i;
              TextView tv = new TextView(this);
              LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 100, 1f);
              lp.setMargins(8, 0, 8, 0);
              tv.setLayoutParams(lp);
              tv.setGravity(Gravity.CENTER);
              tv.setTextSize(28);
              tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
              tv.setFocusable(true);
              tv.setOnClickListener(v -> selecionarLetra(idx));
              tv.setOnFocusChangeListener((v, has) -> { focoOpcao = idx; atualizarFocoOpcoes(); });
              tvOpcaoViews[i] = tv;
              llOpcoes.addView(tv);
          }
          root.addView(llOpcoes);

          setContentView(scroll);
          tvOpcaoViews[0].requestFocus();
      }

      private void carregarPalavra() {
          if (palavraIndex >= ordemPalavras.size()) {
              mostrarFim(true);
              return;
          }
          String[] item = PALAVRAS[ordemPalavras.get(palavraIndex)];
          palavraAtual   = item[0];
          categoriaAtual = item[1];
          dicaAtual      = item[2];
          blankAtual     = 0;
          focoOpcao      = 0;

          // Gera blanks
          int n = palavraAtual.length() <= 3 ? 1 : palavraAtual.length() <= 5 ? 2 : palavraAtual.length() <= 7 ? 3 : (int)(palavraAtual.length() * 0.4);
          List<Integer> indices = new ArrayList<>();
          for (int i = 0; i < palavraAtual.length(); i++) indices.add(i);
          Collections.shuffle(indices);
          blanks = new int[n];
          for (int i = 0; i < n; i++) blanks[i] = indices.get(i);
          Arrays.sort(blanks);

          reveladas = new char[palavraAtual.length()];
          for (int i = 0; i < palavraAtual.length(); i++) {
              boolean ehBlank = false;
              for (int b : blanks) if (b == i) { ehBlank = true; break; }
              reveladas[i] = ehBlank ? 0 : palavraAtual.charAt(i);
          }

          gerarOpcoes();
          atualizarUI();
      }

      private void gerarOpcoes() {
          char correta = palavraAtual.charAt(blanks[blankAtual]);
          List<Character> pool = new ArrayList<>();
          for (char c : ALFABETO.toCharArray()) if (c != correta) pool.add(c);
          Collections.shuffle(pool);

          List<String> lista = new ArrayList<>();
          lista.add(String.valueOf(correta));
          for (int i = 0; i < NUM_OPCOES - 1; i++) lista.add(String.valueOf(pool.get(i)));
          Collections.shuffle(lista);
          for (int i = 0; i < NUM_OPCOES; i++) opcoes[i] = lista.get(i);
      }

      private void atualizarUI() {
          tvProgresso.setText("Palavra " + (palavraIndex + 1) + " / " + PALAVRAS.length);
          tvPontos.setText(pontos + " pts");
          tvVidas.setText(repetir("♥", vidas) + repetir("♡", VIDAS_INICIAIS - vidas));
          tvCategoria.setText(categoriaAtual.toUpperCase());
          tvDica.setText("Dica: " + dicaAtual);

          // Letras
          llLetras.removeAllViews();
          tvLetraViews = new TextView[palavraAtual.length()];
          for (int i = 0; i < palavraAtual.length(); i++) {
              boolean ehBlank = false;
              for (int b : blanks) if (b == i) { ehBlank = true; break; }
              boolean eAtual = ehBlank && blanks[blankAtual] == i;

              TextView tv = new TextView(this);
              LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(52, 60);
              lp.setMargins(4, 0, 4, 0);
              tv.setLayoutParams(lp);
              tv.setGravity(Gravity.CENTER);
              tv.setTextSize(22);
              tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

              if (reveladas[i] != 0) {
                  tv.setText(String.valueOf(reveladas[i]));
                  tv.setTextColor(Color.WHITE);
                  tv.setBackgroundColor(ehBlank ? Color.parseColor("#1A6A1A") : Color.parseColor("#2A2A5A"));
              } else {
                  tv.setText(" ");
                  tv.setTextColor(Color.TRANSPARENT);
                  tv.setBackgroundColor(eAtual ? Color.parseColor("#FFD70040") : Color.parseColor("#1A1A3A"));
              }

              // Borda simulada com padding
              if (eAtual) {
                  tv.setBackgroundColor(Color.parseColor("#3A3A0A"));
              }

              tvLetraViews[i] = tv;
              llLetras.addView(tv);
          }

          // Opções
          for (int i = 0; i < NUM_OPCOES; i++) {
              tvOpcaoViews[i].setText(opcoes[i]);
          }
          atualizarFocoOpcoes();
          tvOpcaoViews[0].requestFocus();
      }

      private void atualizarFocoOpcoes() {
          for (int i = 0; i < NUM_OPCOES; i++) {
              if (i == focoOpcao) {
                  tvOpcaoViews[i].setBackgroundColor(Color.parseColor("#7B2FF7"));
                  tvOpcaoViews[i].setTextColor(Color.WHITE);
              } else {
                  tvOpcaoViews[i].setBackgroundColor(Color.parseColor("#2A2A4A"));
                  tvOpcaoViews[i].setTextColor(Color.parseColor("#C0C0D0"));
              }
          }
      }

      private void selecionarLetra(int opcaoIdx) {
          String escolhida = opcoes[opcaoIdx];
          char correta     = palavraAtual.charAt(blanks[blankAtual]);

          if (escolhida.charAt(0) == correta) {
              tocarSom(1);
              reveladas[blanks[blankAtual]] = correta;
              blankAtual++;

              mostrarFeedback("✓ Certo!", Color.parseColor("#4CAF50"));

              if (blankAtual >= blanks.length) {
                  // Palavra completa
                  pontos += PONTOS_ACERTO;
                  palavraIndex++;
                  handler.postDelayed(() -> carregarPalavra(), 1200);
              } else {
                  gerarOpcoes();
                  handler.postDelayed(() -> atualizarUI(), 500);
              }
          } else {
              tocarSom(2);
              mostrarFeedback("✗ Errado!", Color.parseColor("#FF6B6B"));
              vidas--;
              tvVidas.setText(repetir("♥", vidas) + repetir("♡", VIDAS_INICIAIS - vidas));
              if (vidas <= 0) {
                  handler.postDelayed(() -> mostrarFim(false), 800);
              }
          }
      }

      private void mostrarFeedback(String msg, int cor) {
          tvFeedback.setText(msg);
          tvFeedback.setTextColor(cor);
          tvFeedback.setVisibility(View.VISIBLE);
          handler.postDelayed(() -> tvFeedback.setVisibility(View.INVISIBLE), 800);
      }

      private void mostrarFim(boolean vitoria) {
          if (vitoria) tocarSom(3);
          else tocarSom(4);

          AlertDialog.Builder b = new AlertDialog.Builder(this);
          b.setTitle(vitoria ? "Parabens!" : "Fim de Jogo!");
          b.setMessage((vitoria
              ? "Voce completou todas as " + PALAVRAS.length + " palavras!
"
              : "Voce ficou sem vidas.
") + "Pontuacao: " + pontos + " pts");
          b.setPositiveButton("Jogar de novo", (d, w) -> reiniciar());
          b.setNegativeButton("Sair", (d, w) -> finish());
          b.setCancelable(false);
          b.show();
      }

      private void reiniciar() {
          pontos = 0; vidas = VIDAS_INICIAIS; palavraIndex = 0; blankAtual = 0; focoOpcao = 0;
          Collections.shuffle(ordemPalavras);
          carregarPalavra();
      }

      private void tocarSom(int tipo) {
          if (toneGen == null) return;
          handler.post(() -> {
              try {
                  switch (tipo) {
                      case 0: toneGen.startTone(ToneGenerator.TONE_PROP_BEEP,  60);  break;
                      case 1: toneGen.startTone(ToneGenerator.TONE_PROP_ACK,   200); break;
                      case 2: toneGen.startTone(ToneGenerator.TONE_PROP_NACK,  300); break;
                      case 3: toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT,600); break;
                      case 4: toneGen.startTone(ToneGenerator.TONE_PROP_NACK,  500); break;
                  }
              } catch (Exception ignored) {}
          });
      }

      private static String repetir(String s, int n) {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < n; i++) sb.append(s);
          return sb.toString();
      }

      @Override
      public boolean onKeyDown(int keyCode, KeyEvent event) {
          switch (keyCode) {
              case KeyEvent.KEYCODE_DPAD_LEFT:
                  focoOpcao = (focoOpcao - 1 + NUM_OPCOES) % NUM_OPCOES;
                  atualizarFocoOpcoes();
                  tvOpcaoViews[focoOpcao].requestFocus();
                  return true;
              case KeyEvent.KEYCODE_DPAD_RIGHT:
                  focoOpcao = (focoOpcao + 1) % NUM_OPCOES;
                  atualizarFocoOpcoes();
                  tvOpcaoViews[focoOpcao].requestFocus();
                  return true;
              case KeyEvent.KEYCODE_DPAD_CENTER:
              case KeyEvent.KEYCODE_ENTER:
                  selecionarLetra(focoOpcao);
                  return true;
              case KeyEvent.KEYCODE_BACK:
                  finish();
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
  