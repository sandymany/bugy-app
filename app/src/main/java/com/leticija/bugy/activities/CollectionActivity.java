package com.leticija.bugy.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.auth.LogInActivity;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CollectionActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    Context context;
    String userInsectsResponse;
    Map<String,String> headers;
    JSONArray arrayWithObjects;
    LinearLayout layoutForCardviews;
    LayoutInflater inflater;
    ImageView loading;
    SwipeRefreshLayout swipeRefreshLayout;
    public static int brojac;
    ScrollView scrollView;
    TextView numberOfSpecies;
    ImageView logoImage;


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_collection);

        brojac = 0;
        context = getApplicationContext();
        fragmentManager = getSupportFragmentManager();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //set username on toolbar
        TextView homeText = findViewById(R.id.text_welcome);
        InterfaceFeatures.changeTextViewVisibility(homeText,true, User.username,R.color.dark_green_text);

        //FIND WHAT YOU NEED
        logoImage = findViewById(R.id.logo_image);
        numberOfSpecies = findViewById(R.id.text_count_collection);
        scrollView = findViewById(R.id.scrollView_collection);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout_collection);
        layoutForCardviews = findViewById(R.id.layout_for_cardviews_collection);
        loading = findViewById(R.id.loading_collection);

        //request user insects and display it
        headers = new HashMap<>();
        headers.put("Session-cookie",User.sessionCookie);

        //dok se učitava, postavim loading
        loading.setVisibility(View.VISIBLE);
        InterfaceFeatures.setRotateAnimation(loading);

        TaskQueue.prepare().backgroundTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                userInsectsResponse = Requester.request("/home/getUserCollection",headers,null);
                if (ResponseCheck.isResponseValid(userInsectsResponse,context,fragmentManager)) {
                    System.out.println(userInsectsResponse);
                    try {

                        JSONObject responseObject = new JSONObject(userInsectsResponse);
                        arrayWithObjects = responseObject.getJSONArray("userCollectionSpecies");
                        //na pocetku se svakak loada 5 rezultata (ako ih tolko ima u kolekciji)

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).guiTask(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {
                    InterfaceFeatures.loadMoreCollectionToScrollView(arrayWithObjects,brojac,layoutForCardviews,context,inflater,loading);
                    numberOfSpecies.setText(context.getResources().getString(R.string.collection_number_of_species)+arrayWithObjects.length());

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeMe();

        TaskQueue.prepare().guiTask(new Runnable() {
            @Override
            public void run() {

                loading.clearAnimation();
                loading.setVisibility(View.INVISIBLE);

            }
        }).subscribeMe();

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollView.getChildAt(0).getBottom() == (scrollView.getHeight() + scrollView.getScrollY())) {

                    if (brojac != arrayWithObjects.length()) {
                        InterfaceFeatures.setRotateAnimation(loading);
                        loading.setVisibility(View.VISIBLE);
                        //load more
                            TaskQueue.prepare().backgroundTask(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        InterfaceFeatures.loadMoreCollectionToScrollView(arrayWithObjects,brojac,layoutForCardviews,context,inflater,loading);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).guiTask(new Runnable() {
                                @Override
                                public void run() {
                                    loading.clearAnimation();
                                    loading.setVisibility(View.INVISIBLE);

                                }
                            }).subscribeMe();
                    }
                }
            }
        });


        //SET UP DROPDOWN MENU
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.dropdown_menu_logout);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()==R.id.item_logout) {
                    System.out.println("LOGGING OUT");
                    //send request to log out
                    User.logOut(fragmentManager,context);
                    Intent settingsIntent = new Intent(CollectionActivity.this, LogInActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(settingsIntent);

                } else if (menuItem.getItemId()==R.id.item_view_settings) {
                    //going to ssettings activity
                    Intent intent = new Intent(CollectionActivity.this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }

                return false;
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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("USER WANT TO REFRESH!!");
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }


}
