package com.example.jogomemoria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

/**
 * MainActivity — Tela principal, otimizada para Android TV.
 *
 * Ao abrir o app, o foco é colocado automaticamente no botão "JOGAR"
 * para que o usuário só precise pressionar OK no controle remoto.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnJogar = (Button) findViewById(R.id.btn_jogar);

        // Coloca o foco no botão automaticamente ao abrir a tela
        // (essencial para TV: o controle remoto precisa de um elemento com foco inicial)
        btnJogar.requestFocus();

        // Abre o jogo ao clicar (funciona com toque E com OK do controle remoto)
        btnJogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MemoriaActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Captura o botão OK / Enter do controle remoto na tela principal.
     * Garante que pressionar OK abre o jogo mesmo em TVs com firmware diferente.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            Intent intent = new Intent(MainActivity.this, MemoriaActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
