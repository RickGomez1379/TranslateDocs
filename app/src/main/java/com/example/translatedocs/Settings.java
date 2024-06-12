package com.example.translatedocs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;
import java.util.Objects;

public class Settings extends AppCompatActivity {
    Toolbar nav;
    SharedPreferences preferences;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String preferredLanguage = preferences
                .getString("preferred_languages", Locale.getDefault().getLanguage());
    }

    private String GetLanguageFromCode(String languageCode){
        switch (languageCode){
            case "es":
                return getString(R.string.language_spanish);
            case "de":
                return getString(R.string.language_german);
            case "fr":
                return getString(R.string.language_french);
            case "it":
                return getString(R.string.language_italian);
            case "pt":
                return getString(R.string.language_portuguese);
            case "ro":
                return getString(R.string.language_romanian);
            default:
                return getString(R.string.language_english);
        }
    }
}
