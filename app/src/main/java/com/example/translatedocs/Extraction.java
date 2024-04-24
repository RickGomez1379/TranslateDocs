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

import java.io.IOException;
import java.io.InputStream;


public class Extraction extends Activity {
    TextView extracted;
    ImageView photo;
    Uri imageUri;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extraction_activity);

        photo = findViewById(R.id.userPhoto);
        extracted = findViewById(R.id.extractedText);

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
            e.printStackTrace();
            // Handle any errors that may occur
            return null;
        }
    }


    public void getTextFromImage(Bitmap bitmap){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()){
            Toast.makeText(getApplicationContext(), "Could not get the text", Toast.LENGTH_SHORT).show();
        }
        else{
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();

            SparseArray<TextBlock> items = textRecognizer.detect(frame);

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++){
                TextBlock myItem = items.valueAt(i);
                stringBuilder.append(myItem.getValue());
                stringBuilder.append("\n");
            }
            extracted.setText(stringBuilder.toString());
        }
    }

}
