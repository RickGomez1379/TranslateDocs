package com.example.translatedocs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;


public class Home extends AppCompatActivity {
    final int REQUEST_READ_EXTERNAL_STORAGE = 101;
    final int REQUEST_CAMERA_PERMISSION = 102;
    final int START_CAMERA_CODE = 2;
    CardView galleryCardView;
    CardView translatorCardView;
    CardView photoCardView;
    CardView settingsCardView;
    Toolbar nav;
    String currentPhotoPath;
    TextView recognizerTextView;
    SharedPreferences preferences;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //Assigned CardView Accordingly
        galleryCardView = findViewById(R.id.gallery_Card_View);
        photoCardView = findViewById(R.id.take_Photo_Card_View);
        translatorCardView = findViewById(R.id.translator_Card_View);
        settingsCardView = findViewById(R.id.settings_Card_View);
        recognizerTextView = findViewById(R.id.recognizer_in_use);

        //Setup TopBar
        nav = findViewById(R.id.topbar);
        setSupportActionBar(nav);

        //Choose From Gallery
        galleryCardView.setOnClickListener(v -> {
            //Check if READ_EXTERNAL_STORAGE permissions are granted to Access Gallery
            if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);
            }
            else {
                OpenGallery();
            }
        });

        //Take Photo
        //Navigate to User's Camera
        photoCardView.setOnClickListener(v -> {
            // Check if the CAMERA and WRITE_EXTERNAL_STORAGE permissions are granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // If permissions are not granted, request the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
            }
            else {
                // Permissions already granted, you can proceed with the camera functionality
                OpenCamera();
            }
        });

        //Translator
        //Starts Translator Activity
        translatorCardView.setOnClickListener(v -> StartTranslatorActivity());

        //Starts Setting Activity
        settingsCardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        });
        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        if(preferences.getString("preferred_recognizer", "Latin").equals("Chinese")){
            recognizerTextView.setText("Using Chinese Text Recognizer");
        }
        else if(preferences.getString("preferred_recognizer", "Latin").equals("Japanese")){
            recognizerTextView.setText("Using Japanese Text Recognizer");
        }
        else{
            recognizerTextView.setText("Using Latin Text Recognizer");
        }

    }

    private void StartTranslatorActivity() {
        Intent intent = new Intent(this, TranslatorActivity.class);
        startActivity(intent);
    }

    private void OpenCamera() {
        String fileName = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(this,"com.example.translatedocs", imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, START_CAMERA_CODE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void OpenGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
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
        else if (requestCode == START_CAMERA_CODE && resultCode == RESULT_OK){
            Intent intent = new Intent(this, Extraction.class);
            intent.putExtra("photoBitmap", currentPhotoPath);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission denied, show a message to the user
                // You might want to explain why the permission is needed and ask again
                Toast.makeText(this, "You need to Grant Gallery Permission to Proceed", Toast.LENGTH_SHORT).show();
            } else {
                // Permission granted, open the gallery
                OpenGallery();

            }
        }
        else if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, you can proceed with the camera functionality
                OpenCamera();
            } else {
                // Permissions denied, show a message to the user
                Toast.makeText(this, "Camera permissions are required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }
}