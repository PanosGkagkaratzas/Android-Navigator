package com.example.gpstracker;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView welcome;
    ImageView welcome_icon;
    Animation fromBottom,fromTop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        welcome = findViewById(R.id.welcome_id);
        welcome_icon = findViewById(R.id.welcome_icon);

        fromBottom = AnimationUtils.loadAnimation(this,R.anim.frombottom);
        fromTop = AnimationUtils.loadAnimation(this, R.anim.fromtop);

        welcome.setAnimation(fromBottom);
        welcome_icon.setAnimation(fromTop);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main = new Intent(MainActivity.this, SignIn.class);
                main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(main);
                //finish();
            }
        },4000);
    }
}