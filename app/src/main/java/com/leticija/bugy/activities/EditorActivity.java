package com.leticija.bugy.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leticija.bugy.EditInsectInitializer;
import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.UploadPacker;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;

import org.json.JSONException;


public class EditorActivity extends AppCompatActivity {

    Context context;
    String bugID;
    ImageView insectImage;
    Bitmap nemaSlikeBitmap;
    Button backButton;
    Button uploadButton;
    ImageView loading;
    LinearLayout greenLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_insect_activity);

        context = getBaseContext();
        bugID = Insect.bugId;

        //FIND EVERYTHING YOU
        backButton = findViewById(R.id.backButton_editBug);
        final LinearLayout scrollLayout = findViewById(R.id.linearLayout_scrollView_editBug);
        ScrollView scrollView = findViewById(R.id.scrollView_editBug);
        TextView insectTitle = findViewById(R.id.title_editBug);
        uploadButton = findViewById(R.id.upload_button_edit_insect);
        loading = findViewById(R.id.loading_edit_insect);
        greenLayout = findViewById(R.id.transparentGreen_layout_edit_insect);

        insectImage = findViewById(R.id.editBug_image);
        BitmapDrawable drawable = (BitmapDrawable) insectImage.getDrawable();
        nemaSlikeBitmap = drawable.getBitmap();

        //ACTIVITY ZA PICKANJE SLIKE
        insectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditorActivity.this,ImagePickActivity.class);
                ImagePickActivity.imageToChangeOutside = insectImage;
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //upload textual and photo changes

                greenLayout.setVisibility(View.VISIBLE);
                InterfaceFeatures.setRotateAnimation(loading);

                TaskQueue.prepare().backgroundTask(new Runnable() {
                    @Override
                    public void run() {

                        UploadPacker packer = new UploadPacker(User.sessionCookie,EditInsectInitializer.classGroupTextViews,
                                EditInsectInitializer.aCTextViews,EditInsectInitializer.publicDescEditText.getText().toString(),
                                EditInsectInitializer.notesEditText.getText().toString());

                        BitmapDrawable someBmD = (BitmapDrawable) insectImage.getDrawable();
                        Bitmap imageBitmapNow = someBmD.getBitmap();
                        if (imageBitmapNow.sameAs(nemaSlikeBitmap)) {
                            System.out.println("NEMA SLIKE, NEM UPLOADAL!");
                        } else {
                            System.out.println("UPLOADAM i SLIKU!");
                            packer.packAndUploadPhoto(imageBitmapNow, bugID);
                        }

                        packer.packAndUploadTextualParts(context,getSupportFragmentManager(),bugID);
                    }
                }).guiTask(new Runnable() {
                    @Override
                    public void run() {
                        loading.clearAnimation();
                        greenLayout.setVisibility(View.INVISIBLE);
                        Toast.makeText(context,context.getResources().getString(R.string.toast_upload_successfull), Toast.LENGTH_LONG).show();
                    }
                }).subscribeMe();
            }
        });

        System.out.println("EDITING INSECT ID: "+bugID); //da mogu na server slati ključ na kojem se spremaju promjene

        System.out.println("TITLE IS: "+Insect.getInsectTitle());

        //SET UP SCROLL VIEW
        insectTitle.setText(Insect.getInsectTitle());
        EditInsectInitializer editInsectInitializer = new EditInsectInitializer(scrollView,context);
        try {
            editInsectInitializer.loadClassificationFields(scrollLayout,Insect.getBugData());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        editInsectInitializer.loadPublicDescription(scrollLayout,Insect.getPublicDescription());

        editInsectInitializer.loadNotes(scrollLayout,Insect.getNotes(), bugID);

    }
}
