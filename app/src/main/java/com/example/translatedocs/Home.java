package com.example.translatedocs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.widget.Button;

public class Home extends Activity{
    Button galleryButton;
    Button translatorButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        galleryButton = findViewById(R.id.photoFromGalleryBtn);
        translatorButton = findViewById(R.id.translatorBtn);

        //Navigate to User's Gallery
        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        //Navigate to Translator Screen
        translatorButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TranslatorActivity.class);
            startActivity(intent);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            // Pass the selected image URI to the next activity
            Intent intent = new Intent(Home.this, Extraction.class);
            intent.putExtra("imageUri", selectedImageUri.toString());
            startActivity(intent);
        }
    }
}
