package com.leticija.bugy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.leticija.bugy.net.Requester;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    String TAG = "message";
    String userProperties;
    Context context;
    FragmentManager fragmentManager;
    ImageView logoImage;

    String sessionCookie = User.sessionCookie;
    String password = User.password;
    String username = User.username;
    public static Map<String,String> headersMap = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_home);

        context = getApplicationContext();
        fragmentManager = getSupportFragmentManager();

        //set up dropdown menu
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.dropdown_menu_logout);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()==R.id.item_logout) {
                    System.out.println("LOGGING OUT");
                    //send request to log out
                    User.logOut(fragmentManager,context);
                    Intent settingsIntent = new Intent(HomeActivity.this, LogInActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(settingsIntent);

                } else if (menuItem.getItemId()==R.id.item_view_settings) {
                    //going to ssettings activity
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }

                return false;
            }
        });


    }
    @Override
    protected void onStart() {
        super.onStart();

        // FIND WHAT YOU NEED
        logoImage = findViewById(R.id.logo_image);
        final Button addBugsButton = findViewById(R.id.addBugs_button);
        final Button viewCollectionButton = findViewById(R.id.viewCollection_button);
        final Button settingsButton = findViewById(R.id.settings_button);
        final Button infoButton = findViewById(R.id.info_button);
        final Button helpButton = findViewById(R.id.help_button);

        //postaviti welcome text i user slikicu na toolbaru da je visible
        TextView homeText = findViewById(R.id.text_welcome);
        InterfaceFeatures.changeTextViewVisibility(homeText,true,username,R.color.dark_green_text);

        System.out.println("PASSED SESSION COOKIE: "+sessionCookie);

        // AUTOMATICALLY GET USER PROPERTIES
        headersMap.put("Session-cookie",sessionCookie);

        // pozvati da se dohvacaju propertiesi u pozadini, dok korisnik more bilo kaj raditi
        Requester.wrapInThread("/home/getProperties",headersMap,null,fragmentManager,context);


        // tu se nist nebu ispisalo jer server jos nije nist tak brzo vratil.
        System.out.println("GOT PROPERTIES IN HOME ACTIVITY: "+User.userProperties);

        // BUTTON FUNCTIONALITIES
        addBugsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("ADDBUGS BUTTON CLICKED !");
                InterfaceFeatures.addButtonEffect(context,addBugsButton,R.drawable.add_bugs_clicked,R.drawable.add_bugs,50);
                Intent addBugs = new Intent(context,AddBugsActivity.class);
                startActivity(addBugs);
            }
        });

        viewCollectionButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                System.out.println("VIEW COLLECTION BUTTON CLICKED !");
                InterfaceFeatures.addButtonEffect(context,viewCollectionButton,R.drawable.collection_clicked,R.drawable.collection,50);
                Intent collectionAct = new Intent(context,CollectionActivity.class);
                startActivity(collectionAct);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("SETTINGS BUTTON CLICKED !");
                InterfaceFeatures.addButtonEffect(context,settingsButton,R.drawable.settings_clicked,R.drawable.settings,50);
                Intent settings = new Intent(context,SettingsActivity.class);
                startActivity(settings);

            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("VIEW COLLECTION BUTTON CLICKED !");
                InterfaceFeatures.addButtonEffect(context,infoButton,R.drawable.info_button_clicked,R.drawable.info_button,50);
                Intent info = new Intent(context,InfoActivity.class);
                startActivity(info);
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("VIEW COLLECTION BUTTON CLICKED !");
                InterfaceFeatures.addButtonEffect(context,helpButton,R.drawable.help_button_clicked,R.drawable.help_button,50);
                Intent help = new Intent(context,HelpActivity.class);
                startActivity(help);
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

        Log.i(TAG,"onStart");
    }
    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }
}