package com.example.translatedocs;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;


public class Extraction extends Activity {
    TextView extractedTView;
    TextView translatedTView;
    ImageView photo;
    Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extraction_activity);

        // Assign Views Accordingly
        photo = findViewById(R.id.userPhoto);
        extractedTView = findViewById(R.id.extractedText);
        translatedTView = findViewById(R.id.translatedText);

        // Retrieve the image URI passed from the HomeActivity
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString);
            // Set the selected image to the ImageView
            photo.setImageURI(imageUri);
        }


        // Assuming you have obtained the Bitmap from the previous activity
        Bitmap bitmap = getBitmapFromUri(imageUri);

        // Call the method to process the text from the image
        getTextFromImage(bitmap);
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        try {
            // Use ContentResolver to open an InputStream from the URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            // Decode the InputStream into a Bitmap using BitmapFactory
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // Close the InputStream
            inputStream.close();
            // Return the Bitmap
            return bitmap;
        } catch (IOException e) {

            Toast.makeText(this, e.toString(),Toast.LENGTH_SHORT).show();
            // Handle any errors that may occur
            return null;
        }
    }


    public void getTextFromImage(Bitmap bitmap){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        // Check if the TextRecognizer is operational
        if(!textRecognizer.isOperational()){
            Toast.makeText(getApplicationContext(), "Could not get the text", Toast.LENGTH_SHORT).show();
        }

        else{
            // Create a Frame from the Bitmap
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            // Use the TextRecognizer to detect text in the frame
            SparseArray<TextBlock> items = textRecognizer.detect(frame);

            // Initialize a StringBuilder to store the extracted text
            StringBuilder stringBuilder = new StringBuilder();

            // Iterate through the detected text blocks
            for (int i = 0; i < items.size(); i++){
                TextBlock myItem = items.valueAt(i);
                stringBuilder.append(myItem.getValue());
                stringBuilder.append("\n");
            }
            // Set the extracted text to the TextView
            extractedTView.setText(stringBuilder.toString());

            //Identify Language and Create Translator
            LanguageIdentification.getClient().identifyLanguage(stringBuilder.toString())
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
                        .addOnSuccessListener(unused -> translator.translate(stringBuilder.toString())
                                .addOnSuccessListener(translation -> {

                                    Toast.makeText(Extraction.this, "Successfully Translated",Toast.LENGTH_LONG).show();
                                    translatedTView.setText(translation);

                        }))
                        .addOnFailureListener(e ->
                                Toast.makeText(Extraction.this, "Fail to Download: " + e,Toast.LENGTH_LONG).show());


            });
        }
    }

}
