package com.leticija.bugy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.auth.LogInActivity;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;

public class AddBugsActivity extends AppCompatActivity {

    String TAG = "message";
    FragmentManager fragmentManager;
    Context context;
    ImageView logoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_addbugs);

        fragmentManager = getSupportFragmentManager();
        context = getApplicationContext();

        //SET UP MENU
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.dropdown_menu_logout);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()==R.id.item_logout) {
                    System.out.println("LOGGING OUT");
                    //send request to log out
                    User.logOut(fragmentManager,context);
                    Intent settingsIntent = new Intent(AddBugsActivity.this, LogInActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(settingsIntent);

                } else if (menuItem.getItemId()==R.id.item_view_settings) {
                    //going to ssettings activity
                    Intent intent = new Intent(AddBugsActivity.this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                return false;
            }
        });

        // FIND WHAT YOU NEED
        final ImageView logoImage = findViewById(R.id.logo_image);
        final Button customAddButton = findViewById(R.id.customAdd_button);
        final Button addBySearchButton = findViewById(R.id.addBySearch_button);

        //set user name on screen
        TextView homeText = findViewById(R.id.text_welcome);
        InterfaceFeatures.changeTextViewVisibility(homeText,true,User.username,R.color.dark_green_text);

        // BUTTON FUNCTIONALITIES
        addBySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterfaceFeatures.addButtonEffect(context,addBySearchButton,R.drawable.add_by_search_clicked,R.drawable.add_by_search,50);
                Intent intent = new Intent(AddBugsActivity.this,AddBySearch.class);
                startActivity(intent);
            }
        });
        customAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("CUSTOMADD BUTTON CLICKED !");
                InterfaceFeatures.addButtonEffect(context,customAddButton,R.drawable.custom_add_clicked,R.drawable.custom_add,50);
                Intent intent = new Intent (AddBugsActivity.this, CustomAddActivity.class);
                startActivity(intent);
            }
        });

        logoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.logo_clicked));
                logoImage.setMaxWidth(15);
                Intent infoIntent = new Intent(context,InfoActivity.class);
                startActivity(infoIntent);
                TaskQueue.prepare().backgroundTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).guiTask(new Runnable() {
                    @Override
                    public void run() {
                        logoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.logo_final));

                    }
                }).subscribeMe();
            }
        });


    }
}
