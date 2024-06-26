package com.example.translatedocs;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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
//TODO: Change variable names to avoid/reduce confusion
public class TranslatorActivity extends AppCompatActivity {
    Button translateDownButton;
    Button translateUpButton;
    ImageView firstMic;
    ImageView secondMic;
    Spinner languagesFrom;
    Spinner languagesTo;
    String translateFrom;
    String translateTo;
    TextInputEditText textToTranslateTo;
    TextInputEditText textToTranslateFrom;
    Toolbar nav;
    final int SPEECH_CODE = 102;
    final int REQUEST_MICROPHONE_PERMISSION = 101;
    boolean usingFirstMic = true;
    private StringBuilder recognizedTextBuilder = new StringBuilder();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translator_activity);

        translateDownButton = findViewById(R.id.translate_down_button);
        translateUpButton = findViewById(R.id.translate_up_button);
        firstMic = findViewById(R.id.microphone_start);
        secondMic = findViewById(R.id.microphone_respond);
        languagesFrom = findViewById(R.id.languages_From_Spinner);
        languagesTo = findViewById(R.id.languages_To_Spinner);
        textToTranslateTo = findViewById(R.id.text_To_Translate_To);
        textToTranslateFrom = findViewById(R.id.text_To_Translate);

        //Setup TopBar
        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        firstMic.setOnClickListener(v -> {
            // Check if the RECORD_AUDIO permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                usingFirstMic = true;
                // If permission is not granted, request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_MICROPHONE_PERMISSION);
            }
            else {
                // Permission already granted, proceed with the microphone functionality
                usingFirstMic = true;
                String language = ChosenLanguage(languagesFrom.getSelectedItemPosition());
                OpenMicrophone(language);
            }
        });

        secondMic.setOnClickListener(v -> {
            // Check if the RECORD_AUDIO permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                usingFirstMic = false;
                // If permission is not granted, request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_MICROPHONE_PERMISSION);
            }
            else {
                // Permission already granted, proceed with the microphone functionality
                usingFirstMic = false;
                String language = ChosenLanguage(languagesTo.getSelectedItemPosition());
                OpenMicrophone(language);
            }
        });

        translateDownButton.setOnClickListener(v -> {
            //Set Language Codes
            translateFrom = ChosenLanguage(languagesFrom.getSelectedItemPosition());
            translateTo = ChosenLanguage(languagesTo.getSelectedItemPosition());

            //Translate
            Translate(Objects.requireNonNull(textToTranslateFrom.getText()).toString(), translateFrom,translateTo, textToTranslateTo);
        });
        translateUpButton.setOnClickListener(v -> {
            //Set Language Codes
            translateFrom = ChosenLanguage(languagesTo.getSelectedItemPosition());
            translateTo = ChosenLanguage(languagesFrom.getSelectedItemPosition());

            //Translate
            Translate(Objects.requireNonNull(textToTranslateTo.getText()).toString(), translateFrom,translateTo, textToTranslateFrom);
        });
    }

    private void OpenMicrophone(String language){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");
            startActivityForResult(intent, SPEECH_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_CODE && resultCode == RESULT_OK && data != null){
            String [] result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toArray(new String[0]);
            if(usingFirstMic) {
                //textToTranslateFrom.setText(result[0]);
                // Append the new recognized text to the existing text
                recognizedTextBuilder.append(result[0]).append(" ");
                textToTranslateFrom.setText(recognizedTextBuilder.toString());
                OpenMicrophone(ChosenLanguage(languagesFrom.getSelectedItemPosition()));
            }
            else{
                //textToTranslateTo.setText(result[0]);
                recognizedTextBuilder.append(result[0]).append(" ");
                textToTranslateTo.setText(recognizedTextBuilder.toString());
                OpenMicrophone(ChosenLanguage(languagesTo.getSelectedItemPosition()));
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the microphone functionality
                if(usingFirstMic) {
                    OpenMicrophone(ChosenLanguage(languagesFrom.getSelectedItemPosition()));
                }
                else{
                    OpenMicrophone(ChosenLanguage(languagesTo.getSelectedItemPosition()));
                }
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
    private void Translate(String _textToTranslate, String _from, String _to, TextInputEditText _textTranslated) {
        if(_textToTranslate.isEmpty())
        {
            Toast.makeText(this, "No text to translate. ", Toast.LENGTH_LONG).show();
            return;
        }
        //Download User's Preferred Language Model
        TranslateRemoteModel userModel = new TranslateRemoteModel
                .Builder(Objects.requireNonNull(TranslateLanguage.fromLanguageTag(_to)))
                .build();
        RemoteModelManager manager = RemoteModelManager.getInstance();
        manager.download(userModel, new DownloadConditions
                .Builder()
                .build());

        //Create Translator
        TranslatorOptions options = new TranslatorOptions
                .Builder()
                .setSourceLanguage(_from)
                .setTargetLanguage(_to).build();
        Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions
                .Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> translator.translate(_textToTranslate)
                        .addOnSuccessListener(translation -> {
                            Toast.makeText(TranslatorActivity.this, "Successfully Translated",Toast.LENGTH_LONG).show();
                            _textTranslated.setText(translation);
                                    }))
                .addOnFailureListener(e ->
                        Toast.makeText(TranslatorActivity.this, "Fail to Download: " + e,Toast.LENGTH_LONG).show());
    }
}
