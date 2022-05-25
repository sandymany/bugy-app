package com.leticija.bugy.unused;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.leticija.bugy.R;
import com.leticija.bugy.img.PhotoPacker;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShittyClass {


    String[] languages = { "C","C++","Java","C#","PHP","JavaScript","jQuery","AJAX","JSON" };
    String mCurrentPhotoPath;
    private ImageView imageView;
    Context context;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_insect_activity);

        context = getApplicationContext();
        imageView = findViewById(R.id.imageView_toUpload);
        Button floatingActionButton = findViewById(R.id.Button_insertImage);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(EditorActivity.this);
            }
        });

        /*
        ArrayAdapter<String> adapter = new ArrayAdapter(this,android.R.layout.select_dialog_singlechoice, languages);
        //Find TextView control
        AutoCompleteTextView acTextView = findViewById(R.id.languages);
        //Set the number of characters the user must type before the drop down list is shown
        acTextView.setThreshold(1);
        //Set the adapter
        acTextView.setAdapter(adapter);
        */
}

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add insect picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {

                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(EditorActivity.this, "com.leticija.bugy.fileprovider",photoFile);
                        System.out.println("URI FOR PHOTO: "+photoURI);
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePicture, 0);
                    }

                } else if (options[item].equals("Choose from Gallery")) {

                    if (ActivityCompat.checkSelfPermission(EditorActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditorActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                        System.out.println("DOESNT HAVE PERMISSION.");
                    } else {
                        System.out.println("ALREADY HAVE PERMISSION.");
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent,1);
                    }
                    /*
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
                    */

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {

                        File file = new File(mCurrentPhotoPath);
                        System.out.println("CURRENT PHOTO PATH: "+mCurrentPhotoPath);
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(file));
                            //ImageView imageView = findViewById(R.id.imageView_toUpload);
                            imageView.setImageBitmap(bitmap);

                            System.out.println("HEIGHT: "+imageView.getHeight()+" WIDTH: "+imageView.getHeight());
                            imageView.setRotation(90);

                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            }

                            //trying to upload image to server:
                            List<Bitmap> bmpList = new ArrayList<>();
                            bmpList.add(bitmap);
                            System.out.println("BMP LIST FOR CLASS: "+bmpList);
                            PhotoPacker packer = new PhotoPacker(bmpList);
                            packer.sendPhotosToServerInThread();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case 1:

                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        System.out.println("SELECTED IMAGE URI: "+selectedImage);

                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        System.out.println("FILE PATH COLUMN: "+filePathColumn);

                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                System.out.println("IM HERE");
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);

                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);

                                //ImageView imageView = findViewById(R.id.imageView_toUpload);
                                imageView.setImageBitmap(bitmap);
                                cursor.close();

                                List<Bitmap> bmpList = new ArrayList<>();
                                bmpList.add(bitmap);
                                System.out.println("BMP LIST FOR CLASS: "+bmpList);
                                PhotoPacker packer = new PhotoPacker(bmpList);
                                packer.sendPhotosToServerInThread();
                            }
                        }

                    }
                    break;
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


}

