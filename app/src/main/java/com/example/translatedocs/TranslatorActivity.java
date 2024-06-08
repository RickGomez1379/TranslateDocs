package com.example.translatedocs;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    ImageView mic;
    Spinner languagesFrom;
    Spinner languagesTo;
    String translateFrom;
    String translateTo;
    TextView translationTextView;
    TextInputEditText textToTranslateFrom;
    Toolbar nav;
    final int SPEECH_CODE = 102;
    final int REQUEST_MICROPHONE_PERMISSION = 101;

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

        //Setup TopBar
        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mic.setOnClickListener(v -> {
            // Check if the RECORD_AUDIO permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_MICROPHONE_PERMISSION);
            }
            else {
                // Permission already granted, you can proceed with the microphone functionality
                OpenMicrophone();
            }
        });

        translateButton.setOnClickListener(v -> {
            //Set Language Codes
            translateFrom = ChosenLanguage(languagesFrom.getSelectedItemPosition());
            translateTo = ChosenLanguage(languagesTo.getSelectedItemPosition());

            //Translate
            Translate(Objects.requireNonNull(textToTranslateFrom.getText()).toString(), translateFrom,translateTo);
        });
    }

    private void OpenMicrophone(){
        if(!SpeechRecognizer.isRecognitionAvailable(TranslatorActivity.this)){
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_SHORT).show();
        }
        else {
            String language = ChosenLanguage(languagesFrom.getSelectedItemPosition());

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");
            startActivityForResult(intent, SPEECH_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_CODE && resultCode == RESULT_OK && data != null){
            String [] result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toArray(new String[0]);
            textToTranslateFrom.setText(result[0]);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with the microphone functionality
                OpenMicrophone();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Microphone permission is required to record audio", Toast.LENGTH_SHORT).show();
            }
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
        if(textToTranslate.isEmpty())
        {
            Toast.makeText(this, "No text to translate. ", Toast.LENGTH_LONG).show();
            return;
        }
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
