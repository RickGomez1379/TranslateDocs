package com.example.translatedocs;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
public class Extraction extends AppCompatActivity {
    //UI
    TextView extractedTView;
    TextView translatedTView;
    ImageView imageView;
    Toolbar nav;

    ProgressBar detectedTextProgressBar;
    ProgressBar translatedTextProgressBar;
    Uri imageUri;
    TextRecognizer textRecognizer;
    List<String> originalTexts = new ArrayList<>();
    Map<String,String> translatedTexts = new LinkedHashMap<>();
    SharedPreferences preferences;
    //Add Timer for Consistency
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extraction_activity);

        // Assign UI Accordingly
        imageView = findViewById(R.id.user_Photo);
        extractedTView = findViewById(R.id.extracted_Text);
        translatedTView = findViewById(R.id.translated_Text);
        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        detectedTextProgressBar = findViewById(R.id.detected_text_progress_bar);
        translatedTextProgressBar = findViewById(R.id.translated_text_progress_bar);

        preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String currentRecognizer = preferences.getString("preferred_recognizer", "Latin");

        if (currentRecognizer.equals("Latin")) {
            textRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        }
        else if(currentRecognizer.equals("Japanese")){
            textRecognizer = TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());
        }
        else{
            textRecognizer = TextRecognition.getClient(new ChineseTextRecognizerOptions.Builder().build());
        }

        //If User Used Gallery
        if (getIntent().getStringExtra("galleryUri") != null) {
            // Retrieve Image URI From Home
            String galleryUri = getIntent().getStringExtra("galleryUri");

            //Convert Image from Uri and Set ImageView
            imageUri = Uri.parse(galleryUri);
            imageView.setImageURI(imageUri);

            //Convert Bitmap and Process for Translation
            Bitmap galleryBitmap = getBitmapFromUri(imageUri);
            FirebaseDetectText(galleryBitmap);
        }

        //Else if User decides to use Camera
        else if(getIntent().getStringExtra("photoBitmap") != null ){

            //Extract File and Convert to Bitmap
            String filePath = getIntent().getStringExtra("photoBitmap");
            Bitmap photoBitmap = BitmapFactory.decodeFile(filePath);
            // Set Image to ImageView
            imageView.setImageBitmap(photoBitmap);

            // Process and Translate
            FirebaseDetectText(photoBitmap);
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        try {
            // Open an InputStream from Uri
            InputStream inputStream = getContentResolver().openInputStream(uri);
            // Decode InputStream into Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // Close the InputStream and Return Bitmap
            assert inputStream != null;
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            Toast.makeText(this, e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void FirebaseDetectText(Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    // Successfully Recognized Text
                    if (!visionText.getTextBlocks().isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(Text.TextBlock textBlock : visionText.getTextBlocks()) {
                            //TODO
                            //Append Detected Text to String Builder
                            String text = textBlock.getText();
                            stringBuilder.append(text).append('\n');
                            originalTexts.add(text);
                            Translate(text);
                        }
                        extractedTView.append(stringBuilder.toString().trim());
                    }
                    //If No Text Then set Visibility Accordingly
                    else{
                        translatedTextProgressBar.setVisibility(View.INVISIBLE);
                        translatedTView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Recognition failed
                    Toast.makeText(this, "Text recognition failed: " + e,Toast.LENGTH_LONG).show();
                });
        Runnable runnable = () -> {
            //Switch Visibility
            detectedTextProgressBar.setVisibility(View.INVISIBLE);
            extractedTView.setVisibility(View.VISIBLE);
        };
        handler.postDelayed(runnable, 2000);
    }


    private void Translate(String textToTranslate) {

        if(textToTranslate.isEmpty()){
            Toast.makeText(this, "No Text to Translate", Toast.LENGTH_LONG).show();
            return;
        }

        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyPossibleLanguages(textToTranslate)
                .addOnSuccessListener(identifiedLanguages -> {
                    //Translate Text With Detected Language
                    if (!identifiedLanguages.isEmpty()) {
                        String detectedLanguage = identifiedLanguages.get(0).getLanguageTag();
                        if (!detectedLanguage.equals("und")) { translateText(textToTranslate, detectedLanguage); }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Language identification failed."+ e,Toast.LENGTH_LONG).show());
    }
    private void translateText(String text, String sourceLang) {
        //Get User's Preferred Language
        String preferredLanguage = preferences.getString("preferred_languages", Locale.getDefault().getLanguage());

        //Create Translator Object
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(preferredLanguage)
                .build();
        final Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> translator.translate(text)
                        .addOnSuccessListener(translatedText -> {
                            runOnUiThread(() -> {
                                // Store Translation in Map
                                translatedTexts.put(text, translatedText);
                                // Update translatedTView with Translations
                                updateTranslatedTextView();
                            });
                            //Close Translator
                            translator.close();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Extraction.this, "Translation failed." + e, Toast.LENGTH_LONG).show();
                            translator.close();
                        }))
                .addOnFailureListener(e ->  Toast.makeText(Extraction.this, "Model download failed."+ e,Toast.LENGTH_LONG).show());
    }

    private void updateTranslatedTextView() {
        StringBuilder translatedText = new StringBuilder();
        if(originalTexts.size() == translatedTexts.size()) {
            //Iterate Through All Translations and Append to StringBuilder
            for (String text : originalTexts) {
                String translation = translatedTexts.get(text);
                if (translation != null) {
                    translatedText.append(translation).append("\n");
                }
            }
        }
        //Set Text and Switch Visibility
        translatedTView.setText(translatedText.toString().trim());
        Runnable runnable = () -> {
            translatedTextProgressBar.setVisibility(View.INVISIBLE);
            translatedTView.setVisibility(View.VISIBLE);
        };
        handler.postDelayed(runnable, 2000);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            // Finish the activity and Apply Animation
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        else{ return super.onOptionsItemSelected(item); }
    }
}
