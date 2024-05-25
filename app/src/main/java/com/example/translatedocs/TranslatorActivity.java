package com.example.translatedocs;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.Locale;
import java.util.Objects;

public class TranslatorActivity extends AppCompatActivity {
    Button translateButton;
    ImageView mic;
    Spinner languagesFrom;
    Spinner languagesTo;
    String translateFrom;
    String translateTo;
    TextView translationTextView;
    TextInputEditText textToTranslateFrom;
    Toolbar nav;
    int SPEECH_CODE = 102;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translator_activity);
        translateButton = findViewById(R.id.translateBtn);
        mic = findViewById(R.id.microphoneImageView);
        languagesFrom = findViewById(R.id.languagesFromSpinner);
        languagesTo = findViewById(R.id.languagesToSpinner);
        translationTextView = findViewById(R.id.translationTA);
        textToTranslateFrom = findViewById(R.id.textToTranslate);
        nav = findViewById(R.id.TopBar);

        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mic.setOnClickListener(v -> OpenMicrophone());


        translateButton.setOnClickListener(v -> {
            //Set Language Codes
            translateFrom = ChosenLanguage(languagesFrom.getSelectedItemPosition());
            translateTo = ChosenLanguage(languagesTo.getSelectedItemPosition());

            //Translate
            Translate(Objects.requireNonNull(textToTranslateFrom.getText()).toString(), translateFrom,translateTo);
        });
    }

    private void OpenMicrophone(){
//        if(!SpeechRecognizer.isRecognitionAvailable(TranslatorActivity.this)){
//            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_SHORT).show();
//        }
//        else{}
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");
            startActivityForResult(intent, SPEECH_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_CODE && resultCode == RESULT_OK){

            String [] result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toArray(new String[0]);
            textToTranslateFrom.setText(result[0]);
        }
    }

    //TODO
    private String ChosenLanguage(int language) {
        switch (language){

            case 1:
                return "es";

            case 2:
                return "fr";

            case 3:
                return "de";

            case 4:
                return "pt";

            case 5:
                return "it";

            case 6:
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
