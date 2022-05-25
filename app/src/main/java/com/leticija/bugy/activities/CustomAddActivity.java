package com.leticija.bugy.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.leticija.bugy.EditInsectInitializer;
import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.UploadPacker;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;

import org.json.JSONException;

public class CustomAddActivity extends AppCompatActivity {

    String insectId;
    FragmentManager fragmentManager;
    Button uploadChanges;
    Button backButton;
    String bugId;
    ScrollView scrollView;
    Context context;
    LinearLayout scrollLayout;
    ImageView imageView;
    Bitmap nemaSlikeBitmap;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_add);

        context = getApplicationContext();
        fragmentManager = getSupportFragmentManager();

        imageView = findViewById(R.id.editBug_image);


        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.add_image);
        nemaSlikeBitmap = drawable.getBitmap();

        //FIND WHAT YOU NEED
        final ImageView loading = findViewById(R.id.loading_customAdd);
        final LinearLayout greenLayout = findViewById(R.id.transparentGreen_layout_customAdd);
        imageView = findViewById(R.id.customAdd_image);
        uploadChanges = findViewById(R.id.upload_button_customAdd);
        scrollView = findViewById(R.id.scrollView_customAdd);
        backButton = findViewById(R.id.backButton_customAdd);
        scrollLayout = findViewById(R.id.linearLayout_scrollView_customAdd);

        EditInsectInitializer initializer = new EditInsectInitializer(scrollView,context);

        try {

            initializer.loadClassificationFields(scrollLayout,null);
            initializer.loadPublicDescription(scrollLayout,"");
            initializer.loadNotes(scrollLayout,null,"");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CustomAddActivity.this, ImagePickActivity.class);
                ImagePickActivity.imageToChangeOutside = imageView;
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });

        uploadChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prvo se uploadaju promjene na server. on si sprema stvari i daje kukcu id ... vraća taj ID!!
                greenLayout.setVisibility(View.VISIBLE);
                InterfaceFeatures.setRotateAnimation(loading);

                TaskQueue.prepare().backgroundTask(new Runnable() {
                    @Override
                    public void run() {

                        UploadPacker uploadPacker = new UploadPacker(User.sessionCookie,EditInsectInitializer.classGroupTextViews,EditInsectInitializer.aCTextViews,EditInsectInitializer.publicDescEditText.getText().toString(),null);
                        //server vraća novi id koji je dal insektu
                        insectId = uploadPacker.packAndUploadNewInsect(context,fragmentManager);
                        //dialog pita jel hoćeš dodati u kolekciju
                        System.out.println("RESPONSE: "+insectId);
                        if (insectId.equals("exists")) {
                            System.out.println("OOPS, EXISTS!");
                            InterfaceFeatures.insectExistsInDatabase(context,fragmentManager);
                        } else if (!insectId.equals("")) {
                            // UPLOAD WAS SUCCESSFUL

                            bugId = insectId;
                            System.out.println("ADDING INSECT TO YOUR COLLECTION!");
                            InterfaceFeatures.addInsectToCollectionDialogue(bugId, context, fragmentManager);

                            // SINCE I GOT INSECT ID, I CAN UPLOAD IMAGE
                            BitmapDrawable someBmD = (BitmapDrawable) imageView.getDrawable();
                            Bitmap imageBitmapNow = someBmD.getBitmap();
                            //check if image is changed and upload if it is.
                            if (imageBitmapNow.sameAs(nemaSlikeBitmap)) {
                                System.out.println("NEMA SLIKE, NEM UPLOADAL!");
                            } else {
                                System.out.println("UPLOADAM SLIKU!");
                                uploadPacker.packAndUploadPhoto(imageBitmapNow, insectId);
                            }
                        }

                    }
                }).guiTask(new Runnable() {
                    @Override
                    public void run() {
                        loading.clearAnimation();
                        greenLayout.setVisibility(View.INVISIBLE);
                    }
                }).subscribeMe();

            }
        });

    }
}

