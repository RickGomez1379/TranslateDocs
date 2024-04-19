package com.example.translatedocs;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.Nullable;
import android.Manifest;

public class Home extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
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
}
