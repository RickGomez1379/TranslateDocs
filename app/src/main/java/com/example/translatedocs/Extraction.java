package com.example.translatedocs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
public class Extraction extends AppCompatActivity {
    TextView extractedTView;
    TextView translatedTView;
    ImageView imageView;
    Uri imageUri;
    Toolbar nav;
    TextRecognizer japaneseRecognizer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extraction_activity);

        //Populate Text Recognizer Array
        japaneseRecognizer =
                TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());

        // Assign Views Accordingly
        imageView = findViewById(R.id.userPhoto);
        extractedTView = findViewById(R.id.extractedText);
        translatedTView = findViewById(R.id.translatedText);
        nav = findViewById(R.id.TopBar);

        setSupportActionBar(nav);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Retrieve Image URI passed from Home
        String galleryUri = getIntent().getStringExtra("galleryUri");

        //Retrieve Image Bitmap passed from Home
        Bundle photoExtras = getIntent().getExtras();

        //If User decide to use their Gallery
        if (galleryUri != null) {
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
        else if(photoExtras != null){
            //Extract passed data from Home
            Bitmap photoBitmap = (Bitmap) photoExtras.get("photoBitmap");

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
        TextRecognizer recognizer = japaneseRecognizer;
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    if (!visionText.getTextBlocks().isEmpty()) {
                        // Successfully recognized text
                        SetTextViewAndTranslate(visionText);
                    }
                })
                .addOnFailureListener(e -> {
                    // Recognition failed, try the next recognizer
                });
    }


    private void SetTextViewAndTranslate(Text visionText) {
        // Task completed successfully
        extractedTView.setText(visionText.getText());
        Translate(visionText.getText());
    }


    private void Translate(String textToTranslate) {
        //Identify Language and Create Translator
        LanguageIdentification.getClient().identifyLanguage(textToTranslate)
                .addOnSuccessListener(sourceLanguage -> {

            //Download User's Preferred Language Model
            String userLanguage = Locale.getDefault().getLanguage();
            TranslateRemoteModel userModel = new TranslateRemoteModel
                    .Builder(Objects.requireNonNull(TranslateLanguage.fromLanguageTag(userLanguage)))
                    .build();

            RemoteModelManager manager = RemoteModelManager.getInstance();
            manager.download(userModel, new DownloadConditions
                    .Builder()
                    .build());

            //Create Translator
            TranslatorOptions options = new TranslatorOptions
                    .Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(userLanguage).build();

            Translator translator = Translation.getClient(options);

            DownloadConditions conditions = new DownloadConditions
                    .Builder()
                    .requireWifi()
                    .build();

            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(unused -> translator.translate(textToTranslate)
                            .addOnSuccessListener(translation -> {
                                Toast.makeText(Extraction.this, sourceLanguage,Toast.LENGTH_LONG).show();
                                Toast.makeText(Extraction.this, "Successfully Translated",Toast.LENGTH_LONG).show();
                                translatedTView.setText(translation);

                    }))
                    .addOnFailureListener(e ->
                            Toast.makeText(Extraction.this, "Fail to Download: " + e,Toast.LENGTH_LONG).show());


        });
    }

}
