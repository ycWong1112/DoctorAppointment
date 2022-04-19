package com.example.doctorapointment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 4000;
    private ImageView splashCircle, splashLogoName;
    private ProgressBar splashProgressBar;

    //variable
    Animation topAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make fullscreen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        splashProgressBar = findViewById(R.id.splashProgressBar);
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_navigation);

        splashCircle = findViewById(R.id.splashCircle);
        splashLogoName = findViewById(R.id.splashLogoName);

        splashCircle.setAnimation(topAnim);
        splashLogoName.setAnimation(bottomAnim);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i =0; i<10; i++){
                    splashProgressBar.incrementProgressBy(10);
                    SystemClock.sleep(200);
                }
            }
        });

        thread.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(MainActivity.this, SignUpOrSignInActivity.class);
                startActivity(loginIntent);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}