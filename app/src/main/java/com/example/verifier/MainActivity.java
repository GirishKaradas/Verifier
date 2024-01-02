package com.example.verifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Button button = findViewById(R.id.activity_main_button);
//        button.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
//
        new Handler().postDelayed(() -> {
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

        }, 2000);

    }
}