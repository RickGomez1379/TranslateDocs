package com.example.translatedocs;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class Home extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
    }
}
