package com.example.translatedocs;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;


public class Extraction extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extraction_activity);
        ImageView imageView = findViewById(R.id.userPhoto);

        // Retrieve the image URI passed from the HomeActivity
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            // Set the selected image to the ImageView
            imageView.setImageURI(imageUri);
        }
    }



}
