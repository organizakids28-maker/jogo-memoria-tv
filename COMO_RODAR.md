# Jogo da Memória — Android TV

Aplicativo Android em Java, otimizado para Android TV com controle remoto.

---

## Controles na TV

| Botão do controle | Ação |
|---|---|
| Setas (↑ ↓ ← →) | Mover entre as cartas |
| OK / Enter / Centro | Virar a carta selecionada |
| Voltar | Sair do jogo |

A carta selecionada fica destacada em **amarelo** para facilitar a visualização.

---

## Como baixar o código do Replit

1. No Replit, clique nos **três pontos (...)** no canto superior direito do painel de arquivos
2. Selecione **"Download as zip"**
3. Extraia o arquivo ZIP no seu computador
4. A pasta `jogo-memoria-android/` contém o projeto Android

---

## Como gerar o APK e instalar na TV

> O Replit é um ambiente web e **não consegue compilar código Android** diretamente.
> É necessário compilar no seu computador com o Android Studio (gratuito).

### Passo 1 — Instale o Android Studio (grátis)
- Acesse: https://developer.android.com/studio
- Instale normalmente no Windows, Mac ou Linux

### Passo 2 — Abra o projeto
- Abra o Android Studio
- Clique em **"Open"** (Abrir projeto existente)
- Selecione a pasta `jogo-memoria-android/`
- Aguarde o Gradle sincronizar (pode demorar alguns minutos na primeira vez)

### Passo 3 — Gere o APK
- No menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- Aguarde a compilação
- O APK ficará em: `app/build/outputs/apk/debug/app-debug.apk`

### Passo 4 — Instale na TV

**Opção A — Via pen drive (mais simples):**
1. Copie `app-debug.apk` para um pen drive USB
2. Conecte o pen drive na TV
3. Na TV: Configurações → Segurança → Ative "Fontes desconhecidas"
4. Abra um gerenciador de arquivos na TV (ex: FX File Explorer)
5. Localize e toque no APK para instalar

**Opção B — Via ADB (Wi-Fi, sem fio):**
1. Na TV: Configurações → Sobre → pressione várias vezes em "Build" → Modo Desenvolvedor ativo
2. Ative: Configurações → Opções do desenvolvedor → Depuração ADB / Depuração por rede
3. Descubra o IP da TV: Configurações → Rede → Ver IP
4. No computador, abra o terminal:
   ```
   adb connect IP_DA_TV:5555
   adb install caminho/para/app-debug.apk
   ```
5. O app aparece na grade de apps da TV com o nome "Jogo da Memória"

---

## Estrutura dos arquivos

```
jogo-memoria-android/
├── app/src/main/
│   ├── AndroidManifest.xml          ← Configuração para TV (LEANBACK_LAUNCHER)
│   ├── java/com/example/jogomemoria/
│   │   ├── Carta.java               ← Modelo: valor, virada, encontrada
│   │   ├── MainActivity.java        ← Tela inicial com botão JOGAR
│   │   └── MemoriaActivity.java     ← Jogo: grid 4x4, D-pad, verificação de pares
│   └── res/
│       ├── drawable/
│       │   ├── btn_selector.xml     ← Destaque amarelo quando botão tem foco
│       │   └── banner_tv.xml        ← Banner exibido na tela inicial da TV
│       ├── layout/
│       │   ├── activity_main.xml    ← Tela principal
│       │   └── activity_memoria.xml ← Tela do jogo
│       └── values/
│           ├── strings.xml          ← Textos do app
│           └── styles.xml           ← Tema escuro sem barra de título
```

---

## Requisitos

| Item | Valor |
|---|---|
| Android mínimo | API 17 (Android 4.2) |
| Android alvo | API 21 (Android 5.0) |
| Orientação | Paisagem (landscape) |
| Linguagem | Java puro — sem Kotlin, sem libs externas |
