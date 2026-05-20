package com.example.wordlearningapp;

import android.app.Application;
import com.example.wordlearningapp.util.AuthManager;

public class WordLearningApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 AuthManager
        AuthManager.init(this);
    }
}
