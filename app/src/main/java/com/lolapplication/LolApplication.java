package com.lolapplication;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class LolApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
