package com.example.translatedocs;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
    TextView extractedTView;
    TextView translatedTView;
    ImageView imageView;
    ProgressBar detectedTextProgressBar;
    ProgressBar translatedTextProgressBar;
    Uri imageUri;
    Toolbar nav;
    TextRecognizer textRecognizer;
    List<String> originalTexts = new ArrayList<>();
    Map<String,String> translatedTexts = new LinkedHashMap<>();
    SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extraction_activity);
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



        // Assign Views Accordingly
        imageView = findViewById(R.id.user_Photo);
        extractedTView = findViewById(R.id.extracted_Text);
        translatedTView = findViewById(R.id.translated_Text);
        detectedTextProgressBar = findViewById(R.id.detected_text_progress_bar);
        translatedTextProgressBar = findViewById(R.id.translated_text_progress_bar);

        nav = findViewById(R.id.TopBar);
        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //If User decide to use their Gallery
        if (getIntent().getStringExtra("galleryUri") != null) {
            // Retrieve Image URI passed from Home
            String galleryUri = getIntent().getStringExtra("galleryUri");

            //Convert Image from Uri
            imageUri = Uri.parse(galleryUri);
            // Set Image to ImageView
            imageView.setImageURI(imageUri);
            //Convert Bitmap from Uri
            Bitmap galleryBitmap = getBitmapFromUri(imageUri);

            // Process text from image and translate
            FirebaseDetectText(galleryBitmap);
        }

        //Else if User decides to use Camera
        else if(getIntent().getStringExtra("photoBitmap") != null ){
            String filePath = getIntent().getStringExtra("photoBitmap");

            //Extract passed data from Home
            Bitmap photoBitmap = BitmapFactory.decodeFile(filePath);
            // Set Image to ImageView
            imageView.setImageBitmap(photoBitmap);

            // Process text from image and translate
            FirebaseDetectText(photoBitmap);
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        try {
            // Use ContentResolver to open an InputStream from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            // Decode the InputStream into a Bitmap using BitmapFactory
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // Close the InputStream
            assert inputStream != null;
            inputStream.close();
            // Return the Bitmap
            return bitmap;
        } catch (IOException e) {
            Toast.makeText(this, e.toString(),Toast.LENGTH_SHORT).show();
            // Handle any errors that may occur
            return null;
        }
    }

    public void FirebaseDetectText(Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    if (!visionText.getTextBlocks().isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(Text.TextBlock textBlock : visionText.getTextBlocks()) {
                            // Successfully recognized text
                            String text = textBlock.getText();
                            stringBuilder.append(text).append('\n');
                            originalTexts.add(text);
                            Translate(text);
                        }
                        extractedTView.append(stringBuilder.toString().trim());
                    }})
                .addOnFailureListener(e -> {
                    // Recognition failed, try the next recognizer
                    Toast.makeText(this, "Text recognition failed: " + e,Toast.LENGTH_LONG).show();
                });
        detectedTextProgressBar.setVisibility(View.INVISIBLE);
        extractedTView.setVisibility(View.VISIBLE);
    }


    private void Translate(String textToTranslate) {

        if(textToTranslate.isEmpty()){
            Toast.makeText(this, "No Text to Translate", Toast.LENGTH_LONG).show();
            return;
        }
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();

        languageIdentifier.identifyPossibleLanguages(textToTranslate)
                .addOnSuccessListener(identifiedLanguages -> {
                    if (!identifiedLanguages.isEmpty()) {
                        String detectedLanguage = identifiedLanguages.get(0).getLanguageTag();
                        if (!detectedLanguage.equals("und")) {
                            translateText(textToTranslate, detectedLanguage);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Language identification failed."+ e,Toast.LENGTH_LONG).show());
    }
    private void translateText(String text, String sourceLang) {
        String preferredLanguage = preferences.getString("preferred_languages", Locale.getDefault().getLanguage());

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(preferredLanguage)
                .build();
        final Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> translator.translate(text)
                        .addOnSuccessListener(translatedText -> {
                            runOnUiThread(() -> {
                                // Store the translation in the map
                                translatedTexts.put(text, translatedText);

                                // Update translatedTView with all translations
                                updateTranslatedTextView();
                            });
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
            for (String text : originalTexts) {
                String translation = translatedTexts.get(text);
                if (translation != null) {
                    translatedText.append(translation).append("\n");
                }
            }
        }
        translatedTView.setText(translatedText.toString().trim());
        translatedTextProgressBar.setVisibility(View.INVISIBLE);
        translatedTView.setVisibility(View.VISIBLE);
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
