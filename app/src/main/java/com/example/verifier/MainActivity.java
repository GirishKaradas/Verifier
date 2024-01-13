package com.example.verifier;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends BaseActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.activity_main_imageview);

//        Button button = findViewById(R.id.activity_main_button);
//        button.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
//
        new Handler().postDelayed(() -> {
         //   finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
          //  startActivityForResult(new Intent(MainActivity.this, CameraActivity.class), 101);

        }, 2000);

    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 101 && data != null){
            imageView.setImageURI(Uri.fromFile(new File(data.getStringExtra("file"))));
        }
    }*/
}