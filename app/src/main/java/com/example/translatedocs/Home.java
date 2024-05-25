package com.example.translatedocs;

import android.content.Intent;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;


public class Home extends AppCompatActivity {
    CardView galleryButton;
    CardView translatorButton;
    CardView photoButton;
    Toolbar nav;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //Assigned CardView Accordingly
       galleryButton = findViewById(R.id.galleryCardView);
       photoButton = findViewById(R.id.takePhotoCardView);
       translatorButton = findViewById(R.id.translatorCardView);

       nav = findViewById(R.id.TopBar);

       setSupportActionBar(nav);

        //Choose From Gallery
        //Starts Extraction Activity
        galleryButton.setOnClickListener(v -> {
           Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
       });

        //Take Photo
        //Navigate to User's Camera
        //Starts Extraction Activity
        photoButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, 2);
        });

       //Translator
       //Starts Translator Activity
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
            intent.putExtra("galleryUri", selectedImageUri.toString());
            startActivity(intent);
        }
        else if(requestCode == 2 && resultCode == RESULT_OK && data != null){

            // Photo captured successfully
            Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");

            // Pass the URI of the saved photo file to the next activity
                Intent intent = new Intent(this, Extraction.class);
                intent.putExtra("photoBitmap", photoBitmap);
                startActivity(intent);
        }
    }
}
