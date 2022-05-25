package com.leticija.bugy.activities;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leticija.bugy.R;

public class ExternalLinksActivity extends AppCompatActivity {

    TextView titleInsectName;
    TextView textViewWithLinks;
    Button backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.external_links_activity);

        //FIND WHAT YOU NEED
        backButton = findViewById(R.id.backButton_external_activity);
        titleInsectName = findViewById(R.id.title_external_links);
        textViewWithLinks = findViewById(R.id.text_with_links);

        titleInsectName.setText(getIntent().getExtras().get("insectTitle").toString());
        textViewWithLinks.setText(getIntent().getExtras().get("textWithLinks").toString());

        Linkify.addLinks(textViewWithLinks,Linkify.ALL);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

}
