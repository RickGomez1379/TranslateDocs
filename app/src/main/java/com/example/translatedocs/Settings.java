package com.example.translatedocs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Settings extends AppCompatActivity {
    Toolbar nav;
    SharedPreferences preferences;

    Button saveButton;
    Button resetButton;
    Spinner languageFlagSpinner;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        applySavedLanguage();
        languageFlagSpinner = findViewById(R.id.language_flag_spinner);
        saveButton = findViewById(R.id.save_button);
        resetButton = findViewById(R.id.reset_button);

        List<LanguageItem> languageList = new ArrayList<>();
        languageList.add(new LanguageItem("EN", R.drawable.flag_us));
        languageList.add(new LanguageItem("ES", R.drawable.flag_mex));
        languageList.add(new LanguageItem("FR", R.drawable.flag_fr));
        languageList.add(new LanguageItem("DE", R.drawable.flag_de));
        languageList.add(new LanguageItem("IT", R.drawable.flag_it));
        languageList.add(new LanguageItem("PO", R.drawable.flag_br));
        languageList.add(new LanguageItem("RO", R.drawable.flag_ro));

        LanguageSpinnerAdapter adapter = new LanguageSpinnerAdapter(this, languageList);
        languageFlagSpinner.setAdapter(adapter);

        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String preferredLanguage = preferences
                .getString("preferred_languages",Locale.getDefault().getLanguage());

        int spinnerPosition = getSpinnerPosition(adapter,preferredLanguage);
        languageFlagSpinner.setSelection(spinnerPosition);

        saveButton.setOnClickListener(v -> {
            LanguageItem current = (LanguageItem) languageFlagSpinner.getSelectedItem();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("preferred_languages", current.getLanguageCode().toLowerCase());
            editor.apply();
            
            setLocale(current.getLanguageCode().toLowerCase());
            String message = getString(R.string.language_saved_message, current.getLanguageCode().toLowerCase());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        resetButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Reset to default locale
            setLocale(Locale.getDefault().getLanguage());
            Toast.makeText(this, getString(R.string.preference_reset), Toast.LENGTH_SHORT).show();
        });
    }

    private void setLocale(String _languageCode) {
        Locale locale = new Locale(_languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Refresh activity to apply the new language
        Intent refresh = new Intent(this, Settings.class);
        startActivity(refresh);
        finish();
    }
    private void applySavedLanguage() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String languageCode = sharedPreferences.getString("preferred_languages", Locale.getDefault().getLanguage());
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private int getSpinnerPosition(LanguageSpinnerAdapter _adapter, String language){
        for (int i = 0; i < _adapter.getCount(); i++) {
            if (_adapter.getItem(i).getLanguageCode().equals(language.toUpperCase())) {
                return i;
            }
        }
        return 0; // Value not found // Value not found
    }

}
