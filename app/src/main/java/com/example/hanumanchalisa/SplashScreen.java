package com.example.hanumanchalisa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);this.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_splash_screen);
        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep( 1000 );
                } catch (Exception e) {
                    Toast.makeText(SplashScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    startActivity( new Intent( SplashScreen.this, MainActivity.class ) );
                    finish();
                }
            }        };
        thread.start();
    }
}