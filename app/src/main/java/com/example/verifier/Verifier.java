package com.example.verifier;

import android.app.Application;

import com.koushikdutta.ion.Ion;

public class Verifier extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Ion.getDefault(this).getConscryptMiddleware().enable(false);

    }
}
