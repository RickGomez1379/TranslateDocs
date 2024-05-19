package com.example.translatedocs;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Objects;

public class TranslatorActivity extends AppCompatActivity {
    Button translateButton;
    Spinner languagesFrom;
    Spinner languagesTo;
    String translateFrom;
    String translateTo;
    TextView translationTextView;
    TextInputEditText textToTranslateFrom;
    Toolbar nav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translator_activity);
        translateButton = findViewById(R.id.translateBtn);
        languagesFrom = findViewById(R.id.languagesFromSpinner);
        languagesTo = findViewById(R.id.languagesToSpinner);
        translationTextView = findViewById(R.id.translationTA);
        textToTranslateFrom = findViewById(R.id.textToTranslate);
        nav = findViewById(R.id.TopBar);

        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        translateButton.setOnClickListener(v -> {
            //Set Language Codes
            translateFrom = ChosenLanguage(languagesFrom.getSelectedItem().toString());
            translateTo = ChosenLanguage(languagesTo.getSelectedItem().toString());

            //Translate
            Translate(Objects.requireNonNull(textToTranslateFrom.getText()).toString(), translateFrom,translateTo);
        });
    }

    private String ChosenLanguage(String language) {
        switch (language){

            case "Spanish":
                return "es";

            case "French":
                return "fr";

            case "Germany":
                return "de";

            case "Portuguese":
                return "pt";

            case "Italian":
                return "it";

            case "Romanian":
                return "ro";

            default:
                return "en";
        }
    }
    private void Translate(String textToTranslate, String from, String to) {

                    //Download User's Preferred Language Model
                    TranslateRemoteModel userModel = new TranslateRemoteModel
                            .Builder(Objects.requireNonNull(TranslateLanguage.fromLanguageTag(to)))
                            .build();

                    RemoteModelManager manager = RemoteModelManager.getInstance();
                    manager.download(userModel, new DownloadConditions
                            .Builder()
                            .build());

                    //Create Translator
                    TranslatorOptions options = new TranslatorOptions
                            .Builder()
                            .setSourceLanguage(from)
                            .setTargetLanguage(to).build();

                    Translator translator = Translation.getClient(options);

                    DownloadConditions conditions = new DownloadConditions
                            .Builder()
                            .requireWifi()
                            .build();

                    translator.downloadModelIfNeeded(conditions)
                            .addOnSuccessListener(unused -> translator.translate(textToTranslate)
                                    .addOnSuccessListener(translation -> {

                                        Toast.makeText(TranslatorActivity.this, "Successfully Translated",Toast.LENGTH_LONG).show();
                                        translationTextView.setText(translation);

                                    }))
                            .addOnFailureListener(e ->
                                    Toast.makeText(TranslatorActivity.this, "Fail to Download: " + e,Toast.LENGTH_LONG).show());


    }
}
