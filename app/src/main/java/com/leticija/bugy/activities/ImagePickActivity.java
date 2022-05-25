package com.leticija.bugy.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.leticija.bugy.R;
import com.theartofdev.edmodo.cropper.CropImage;

public class ImagePickActivity extends AppCompatActivity {

    public static ImageView imageToChangeOutside;
    ImageView imageView;
    Button chooseImageButton;
    Context context;
    Button removeImageButton;
    Button backButton;
    Button doneButton;
    Uri resultUri;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_picker_activity);

        context= getApplicationContext();

        //FIND WHAT YOU NEED
        doneButton = findViewById(R.id.button_doneImagePick);
        backButton = findViewById(R.id.backButton_imagePick);
        imageView = findViewById(R.id.picked_image);
        chooseImageButton = findViewById(R.id.pickImage_button);
        removeImageButton = findViewById(R.id.remove_image_button_imagePick);

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity().start(ImagePickActivity.this);

            }
        });

        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageToChangeOutside.setImageDrawable(getResources().getDrawable(R.drawable.add_image));
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (resultUri != null) {
                    imageToChangeOutside.setImageURI(resultUri);
                }
                onBackPressed();

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //dok se slika postavila, kasnije ju uzeti(bmp) i slati na server
                resultUri = result.getUri();
                imageView.setImageURI(resultUri);
                System.out.println("IM HERE !!!! ONACTIVITY RESULT");

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(context,"OOPS, SOMETHING HAPPENED.",Toast.LENGTH_SHORT).show();
            }
        }

    }
}
