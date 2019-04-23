package com.blank.writotext.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.blank.writotext.R;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        ImageView imageView = findViewById(R.id.splashview);
        TextView textView = findViewById(R.id.text2);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
        imageView.startAnimation(animation);textView.startAnimation(animation);



        Thread timer = new Thread(){

            @Override
            public void run() {

                try {
                    sleep(500);
                    Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(Splash.this,
                            R.anim.fade_in, R.anim.fade_out).toBundle();
                    startActivity(intent, bundle);
                    finish();
                    super.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        };

        timer.start();
    }
}
