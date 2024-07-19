package com.example.translatedocs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.ImageButton;
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
import java.util.Locale;
import java.util.Objects;


public class Home extends AppCompatActivity {
    // Request/Activity Codes
    final int REQUEST_READ_EXTERNAL_STORAGE = 101;
    final int REQUEST_CAMERA_PERMISSION = 102;
    final int START_CAMERA_CODE = 2;
    final int START_GALLERY_CODE = 1;
    //CardViews and TextViews
    CardView galleryCardView;
    CardView translatorCardView;
    CardView photoCardView;
    CardView settingsCardView;
    TextView galleryText;
    TextView photoText;
    TextView translatorText;
    TextView settingsText;
    //User's Preferences
    SharedPreferences preferences;
    //TopBar
    Toolbar nav;
    String currentPhotoPath;
    TextView recognizerTextView;
    //Reset Image View
    ImageButton reset;

    //User's Preferences and Saved Them in String for Later Use
    String recognizerPreference;
    String userCurrentLanguageCode;
    String userDeviceLanguageCode;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        //User's Preferences (Language, Text Detector)
        preferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        recognizerPreference = preferences.getString("preferred_recognizer", "Latin");
        userCurrentLanguageCode = preferences.getString("preferred_languages", "EN");
        userDeviceLanguageCode = Locale.getDefault().getLanguage();

        //Assigned CardView Accordingly
        galleryCardView = findViewById(R.id.gallery_Card_View);
        galleryText = findViewById(R.id.gallery_text_view);
        photoCardView = findViewById(R.id.take_Photo_Card_View);
        photoText = findViewById(R.id.take_photo_text_view);
        translatorCardView = findViewById(R.id.translator_Card_View);
        translatorText = findViewById(R.id.translator_text_view);
        settingsCardView = findViewById(R.id.settings_Card_View);
        settingsText = findViewById(R.id.settings_text_view);

        recognizerTextView = findViewById(R.id.recognizer_in_use);
        reset = findViewById(R.id.home_reset);

        //Setup TopBar
        nav = findViewById(R.id.topbar);
        setSupportActionBar(nav);

        //Sets OnClick Listeners for CardViews
        SetupCardViewListeners();

        //Set TextView For User's Knowledge on What Recognizer TranslateDocs is Using
        SetTextRecognizerTextView();

        //TODO: Accessibility
        reset.setOnTouchListener((v, event) -> {
            Resources res = getResources();
            //Changes Language Accordingly Depending if Pressed or Released
            switch (event.getAction()) {
                //When Pressed Down
                case MotionEvent.ACTION_DOWN:
                    setLocale(userDeviceLanguageCode);
                    SetTextViews(res);
                    SetTextRecognizerTextView();
                    return true;
                    //When Button is Released
                case MotionEvent.ACTION_UP:
                    setLocale(userCurrentLanguageCode);
                    SetTextViews(res);
                    SetTextRecognizerTextView();
                    return true;
            }
            return false;
        });


    }

    //Set Text of the Text Views, Used When User Press Reset Button
    private void SetTextViews(Resources res) {
        galleryText.setText(res.getString(R.string.choose_picture_from_gallery));
        photoText.setText(res.getString(R.string.take_photo));
        translatorText.setText(res.getString(R.string.translator));
        settingsText.setText(res.getString(R.string.settings));
    }

    //Set Locale Language, Used When User Press Reset Button
    private void setLocale(String _languageCode) {
        Locale locale = new Locale(_languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    //Set TextView For User's Knowledge on What Recognizer TranslateDocs is Using
    private void SetTextRecognizerTextView() {

        if(recognizerPreference.equals("Chinese")){
            recognizerPreference = getString(R.string.using_chinese_recognizer);
        }
        else if(recognizerPreference.equals("Japanese")){
            recognizerPreference = getString(R.string.using_japanese_recognizer);
        }
        else{
            recognizerPreference = getString(R.string.using_latin_recognizer);
        }
        recognizerTextView.setText(recognizerPreference);
    }

    //Sets OnClick Listeners for CardViews
    private void SetupCardViewListeners() {
        //Choose From Gallery
        galleryCardView.setOnClickListener(v -> {
            //Check if READ_EXTERNAL_STORAGE permissions are granted to Access Gallery
            if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);
            }
            // Permissions already granted, Open Gallery Intent
            else { OpenGallery(); }
        });

        //Listener to Start User's Camera Intent
        photoCardView.setOnClickListener(v -> {
            // Check if the CAMERA and WRITE_EXTERNAL_STORAGE permissions are granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // If permissions are not granted, request the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
            }
            // Permissions already granted, Open Camera Intent
            else { OpenCamera(); }
        });
        //Translator
        //Starts Translator Activity
        translatorCardView.setOnClickListener(v -> StartTranslatorActivity());

        //Settings
        //Starts Setting Activity
        settingsCardView.setOnClickListener(v -> StartSettingsActivity());
    }

    private void StartSettingsActivity() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
        //Finish Activity, In Case Settings has Changed
        finish();
        //Animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void StartTranslatorActivity() {
        Intent intent = new Intent(this, TranslatorActivity.class);
        startActivity(intent);

        //Animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void OpenCamera() {
        String fileName = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            //Create File for ImageUri
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(this,"com.example.translatedocs", imageFile);

            //Starts Camera Intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, START_CAMERA_CODE);

            //Animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void OpenGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, START_GALLERY_CODE);

        //Animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Gallery Result
        if (requestCode == START_GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            // Pass the Selected ImageUri to Extraction Activity
            Uri selectedImageUri = data.getData();
            Intent intent = new Intent(Home.this, Extraction.class);
            intent.putExtra("galleryUri", Objects.requireNonNull(selectedImageUri).toString());
            startActivity(intent);

            //Animation
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        //Camera Result
        else if (requestCode == START_CAMERA_CODE && resultCode == RESULT_OK){
            // Pass the Image File Path to Extraction Activity
            Intent intent = new Intent(this, Extraction.class);
            intent.putExtra("photoBitmap", currentPhotoPath);
            startActivity(intent);

            //Animation
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission denied, show a message to the user
                // Explain Why User need to Grant Permission
                Toast.makeText(this, "Gallery permissions are required to choose picture from gallery", Toast.LENGTH_SHORT).show();
            }
            // Permission granted, open the gallery
            else { OpenGallery(); }
        }
        else if (requestCode == REQUEST_CAMERA_PERMISSION){
            // Permissions granted, proceed with the camera functionality
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { OpenCamera(); }
            else {
                // Permissions denied, show a message to the user
                Toast.makeText(this, "Camera permissions are required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }
}