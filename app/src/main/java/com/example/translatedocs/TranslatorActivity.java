package com.example.translatedocs;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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

import java.util.Locale;
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
    TextInputEditText bottomTextToTranslate;
    TextInputEditText topTextToTranslate;
    Toolbar nav;
    final int SPEECH_CODE = 102;
    final int REQUEST_MICROPHONE_PERMISSION = 101;
    boolean usingFirstMic = true;
    private final StringBuilder recognizedTextBuilder = new StringBuilder();
    ImageView topSpeaker;
    ImageView bottomSpeaker;
    TextToSpeech tts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translator_activity);

        translateDownButton = findViewById(R.id.translate_down_button);
        translateUpButton = findViewById(R.id.translate_up_button);
        firstMic = findViewById(R.id.microphone_top);
        secondMic = findViewById(R.id.microphone_bottom);
        languagesFrom = findViewById(R.id.top_languages_spinner);
        languagesTo = findViewById(R.id.bottom_languages_spinner);
        bottomTextToTranslate = findViewById(R.id.bottom_text_to_translate);
        topTextToTranslate = findViewById(R.id.top_text_to_translate);
        topSpeaker = findViewById(R.id.speaker_top);
        bottomSpeaker = findViewById(R.id.speaker_bottom);

        //Setup TopBar
        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Top Mic Button Click
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

        //Bottom Mic Button Click
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

        //Top Speaker Icon Click
        topSpeaker.setOnClickListener(v -> {

            //if Text View is not Empty, Set-up and Active Text-To-Speech
            if(!Objects.requireNonNull(topTextToTranslate.getText()).toString().isEmpty()){
                //Initialize Text-To-Speech with Bottom Language Spinner and Speaks
                tts = new TextToSpeech(this, status -> {
                    tts.setLanguage(Locale.forLanguageTag((ChosenLanguage(languagesFrom.getSelectedItemPosition()))));
                    tts.speak(topTextToTranslate.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                });
            }
        });

        //Bottom Speaker Icon Click
        bottomSpeaker.setOnClickListener(v -> {

            //if Text View is not Empty, Set-up and Active Text-To-Speech
            if(!Objects.requireNonNull(bottomTextToTranslate.getText()).toString().isEmpty()){
                //Initialize Text-To-Speech with Bottom Language Spinner and Speaks
                tts = new TextToSpeech(this, status -> {
                    tts.setLanguage(Locale.forLanguageTag((ChosenLanguage(languagesTo.getSelectedItemPosition()))));
                    tts.speak(bottomTextToTranslate.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                });
            }
        });

        //Translate Button w/ Down Arrow Icon
        translateDownButton.setOnClickListener(v -> {
            //Set Language Codes
            translateFrom = ChosenLanguage(languagesFrom.getSelectedItemPosition());
            translateTo = ChosenLanguage(languagesTo.getSelectedItemPosition());

            //Translate
            Translate(Objects.requireNonNull(topTextToTranslate.getText()).toString(), translateFrom,translateTo, bottomTextToTranslate);
        });
        translateUpButton.setOnClickListener(v -> {
            //Set Language Codes
            translateFrom = ChosenLanguage(languagesTo.getSelectedItemPosition());
            translateTo = ChosenLanguage(languagesFrom.getSelectedItemPosition());

            //Translate
            Translate(Objects.requireNonNull(bottomTextToTranslate.getText()).toString(), translateFrom,translateTo, topTextToTranslate);
        });
    }

    private void OpenMicrophone(String language){
            //Initialize Mic Intent
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //Set Language to Listen From
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");
            //Starts Intent
            startActivityForResult(intent, SPEECH_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if no Error Starts Microphone Intent
        if (requestCode == SPEECH_CODE && resultCode == RESULT_OK && data != null){
            String [] result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toArray(new String[0]);

            if(usingFirstMic) {
                //textToTranslateFrom.setText(result[0]);
                // Append the new recognized text to the existing text
                recognizedTextBuilder.append(result[0]).append(" ");
                topTextToTranslate.setText(recognizedTextBuilder.toString());
                OpenMicrophone(ChosenLanguage(languagesFrom.getSelectedItemPosition()));
            }
            else{
                //textToTranslateTo.setText(result[0]);
                recognizedTextBuilder.append(result[0]).append(" ");
                bottomTextToTranslate.setText(recognizedTextBuilder.toString());
                OpenMicrophone(ChosenLanguage(languagesTo.getSelectedItemPosition()));
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //If Trying to Request Mic Permission
        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {

            //If Granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Depending on With Mic User Wants to Use
                if(usingFirstMic) {
                    OpenMicrophone(ChosenLanguage(languagesFrom.getSelectedItemPosition()));
                }
                else{
                    OpenMicrophone(ChosenLanguage(languagesTo.getSelectedItemPosition()));
                }
                //Else Not Granted
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Microphone permission is required to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Converts Spinner Item Position(int) to Their Associated Language Code
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
            case 7:
                return "ja";
            case 8:
                return "zh";
            case 9:
                return "ru";
            case 10:
                return "ar";
            case 11:
                return "hi";
            default:
                return "en";
        }
    }


    //Translate (Text to Translate From, Language to Translate From, Language to Translate To, Text to Modify with Translated Text)
    private void Translate(String _textToTranslate, String _from, String _to, TextInputEditText _textTranslated) {
        //If There is no Text to Translate
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

        //Translate And Set Text
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
