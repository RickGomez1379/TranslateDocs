package com.example.translatedocs;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.Nullable;
import android.Manifest;
import android.widget.Button;

public class Home extends Activity{
    Button galleryButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        galleryButton = findViewById(R.id.photoFromGalleryBtn);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
    }

    //TODO: Once Prompted, CLOSES APPLICATION but able to access camera once allowed
    public void doProcess(View view) {
        //Check app level permission is granted for Camera
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //Grant the permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }

            //Open the camera => create an Intent object
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 101);

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
