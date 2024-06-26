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
    Spinner textRecognizerSpinner;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        applySavedLanguage();
        languageFlagSpinner = findViewById(R.id.language_flag_spinner);
        textRecognizerSpinner = findViewById(R.id.text_recognizer_spinner);
        saveButton = findViewById(R.id.save_button);
        resetButton = findViewById(R.id.reset_button);

        List<IconAndStringItem> languageList = new ArrayList<>();
        List<IconAndStringItem> textRecognizer = new ArrayList<>();
        languageList.add(new IconAndStringItem("EN", R.drawable.flag_us));
        languageList.add(new IconAndStringItem("ES", R.drawable.flag_mex));
        languageList.add(new IconAndStringItem("FR", R.drawable.flag_fr));
        languageList.add(new IconAndStringItem("DE", R.drawable.flag_de));
        languageList.add(new IconAndStringItem("IT", R.drawable.flag_it));
        languageList.add(new IconAndStringItem("PO", R.drawable.flag_br));
        languageList.add(new IconAndStringItem("RO", R.drawable.flag_ro));



        textRecognizer.add(new IconAndStringItem("Latin", R.drawable.latin_a));
        textRecognizer.add(new IconAndStringItem("Japanese", R.drawable.hiragana_a));
        textRecognizer.add(new IconAndStringItem("Chinese", R.drawable.chinese_che));

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, languageList);
        CustomSpinnerAdapter textAdapter = new CustomSpinnerAdapter(this, textRecognizer);

        languageFlagSpinner.setAdapter(adapter);
        textRecognizerSpinner.setAdapter(textAdapter);

        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String preferredLanguage = preferences
                .getString("preferred_languages",Locale.getDefault().getLanguage());
        String preferredRecognizer = preferences
                .getString("preferred_recognizer", "Latin");

        //Set Spinner Position to Saved Preferences
        int spinnerPosition = getSpinnerPosition(adapter,preferredLanguage);
        languageFlagSpinner.setSelection(spinnerPosition);
        spinnerPosition = getSpinnerPosition(textAdapter, preferredRecognizer);
        textRecognizerSpinner.setSelection(spinnerPosition);

        saveButton.setOnClickListener(v -> {
            IconAndStringItem current = (IconAndStringItem) languageFlagSpinner.getSelectedItem();
            IconAndStringItem currentRecognizer = (IconAndStringItem) textRecognizerSpinner.getSelectedItem();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("preferred_languages", current.getString().toLowerCase());
            editor.putString("preferred_recognizer", currentRecognizer.getString());
            editor.apply();
            
            setLocale(current.getString().toLowerCase());
            String message = getString(R.string.language_saved_message, current.getString().toLowerCase());
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

    private int getSpinnerPosition(CustomSpinnerAdapter _adapter, String language){
        for (int i = 0; i < _adapter.getCount(); i++) {
            if (_adapter.getItem(i).getString().toUpperCase().equals(language.toUpperCase())) {
                return i;
            }
        }
        return 0; // Value not found // Value not found
    }

}
