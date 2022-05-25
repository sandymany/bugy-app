package com.leticija.bugy.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leticija.bugy.EditInsectInitializer;
import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.InsectInfoInitializer;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BugInfoActivity extends AppCompatActivity {

    FragmentManager fragmentManager = getSupportFragmentManager();
    String images;
    JSONObject bugData;
    Context context;
    String bugId;
    Map<String,String> headersMap;
    String responseMakniKukca;
    SwipeRefreshLayout swipeRefreshLayout;
    String bugDataResponse;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bug_info_activity);

        TaskQueue.prepare().backgroundTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                headersMap = new HashMap<>();
                headersMap.put("Session-cookie",User.sessionCookie);
                User.userProperties = User.requestUserProperties(context,fragmentManager, (HashMap) headersMap);
                headersMap.put("Insect-id",bugId);
                Insect.publicDescription = Requester.request("/home/getPublicDescription",headersMap,null);
                System.out.println("REQUESTED PUBLIC DESCRIPTION AT BEGINNING OF BUGINFOACTIVITY: "+Insect.publicDescription);
            }
        }).subscribeMe();


        //clear INSECT cache

        Insect.clearCache();
        context = getApplicationContext();

        // FIND WHAT YOU NEED
        final ImageView loading = findViewById(R.id.loading_bugInfo);
        final ImageView bugImage = findViewById(R.id.bugInfo_image);
        final Button backButton = findViewById(R.id.backButton_bugInfo);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout_bugInfo);
        final LinearLayout scrollLinearLayout = findViewById(R.id.linearLayout_scrollView_bugInfo);
        TextView mainTitle = findViewById(R.id.title_bugInfo);
        ScrollView scrollView = findViewById(R.id.scrollView_bugInfo);
        scrollView.setVisibility(View.INVISIBLE);
        CardView bugImageCardview = findViewById(R.id.bugInfo_cardView);

        final InsectInfoInitializer myScrollView = new InsectInfoInitializer(scrollView, context);
        final Intent pastIntent = getIntent();

        try {
            String bugDataString = pastIntent.getStringExtra("data");
            Insect.setBugData(bugDataString); //caching so i can use that later in activities.
            System.out.println("BUGDATA: "+Insect.bugData);

            bugData = new JSONObject(bugDataString);
            if (bugData.has("IMAGES")) {
                images = bugData.get("IMAGES").toString();
            }

            final String textForTitle = (bugData.get("GENUS").toString()) + " " + bugData.get("SPECIES").toString();

            //postavi text na danom titleu
            myScrollView.setTitle(textForTitle,mainTitle);
            //caching
            Insect.setInsectTitle(textForTitle);
            //postavljanje slike
            myScrollView.setImage(bugData, loading, bugImage);
            //loadanje klasifikacije
            myScrollView.loadInsectClassification(bugData,scrollLinearLayout);
            //loadanje public descriptiona
            bugId = bugData.get("KEY").toString();
            myScrollView.loadPublicDescription(bugId,scrollLinearLayout,fragmentManager);
            //caching
            Insect.bugId = bugId;

            //loadanje notesa
            myScrollView.loadNotes(bugId,context,fragmentManager,scrollLinearLayout);

            //inflate dropdown menu to invisible toolbar
            final Toolbar toolbar = findViewById(R.id.toolbar_bugInfo);
            toolbar.inflateMenu(R.menu.dropdown_menu_insect_info);

            TaskQueue.prepare().guiTask(new Runnable() {
                @Override
                public void run() {
                    MenuItem itemAddOrRemoveInsect = toolbar.getMenu().findItem(R.id.item_add_remove_insect);

                    if (User.isInsectInCollection(bugId,User.userProperties)) {
                        System.out.println("USER ALREADY OWNS THE INSECT!");
                        itemAddOrRemoveInsect.setTitle(context.getResources().getString(R.string.remove_from_collection));
                    }
                    else {
                        System.out.println("USER DOESNT OWN INSECT!");
                        itemAddOrRemoveInsect.setTitle(context.getResources().getString(R.string.add_to_collection));
                    }
                }
            }).subscribeMe();

            headersMap = new HashMap<>();
            headersMap.put("Session-cookie",User.sessionCookie);

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if(item.getItemId()==R.id.item_add_remove_insect)
                    {
                        //headersMap.put("Insect-id",bugId);

                        if (User.isInsectInCollection(bugId,User.userProperties)) {
                            //request serveru da makne kukca s kolekcije
                            TaskQueue.prepare().backgroundTask(new Runnable() {
                                @Override
                                public void run() {

                                    responseMakniKukca = Requester.request("/home/removeFromCollection",headersMap,null);

                                }
                            }).guiTask(new Runnable() {
                                @Override
                                public void run() {

                                    if(ResponseCheck.isResponseValid(responseMakniKukca,context,fragmentManager)) {
                                        Toast.makeText(context, context.getResources().getString(R.string.toast_removing_from_collection), Toast.LENGTH_SHORT).show();
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                    }

                                }
                            }).subscribeMe();

                        }
                        else {
                            //request serveru da doda kukca u kolekciju
                            TaskQueue.prepare().backgroundTask(new Runnable() {
                                @Override
                                public void run() {

                                    responseMakniKukca = Requester.request("/home/addToCollection",headersMap,null);

                                }
                            }).guiTask(new Runnable() {
                                @Override
                                public void run() {

                                    if(ResponseCheck.isResponseValid(responseMakniKukca,context,fragmentManager)) {
                                        Toast.makeText(context, context.getResources().getString(R.string.toast_adding_to_collection), Toast.LENGTH_SHORT).show();
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                    }

                                }
                            }).subscribeMe();
                        }
                    }
                    else if (item.getItemId() == R.id.item_edit_insect)
                    {
                        Toast.makeText(context, context.getResources().getString(R.string.toast_editing_insect), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(BugInfoActivity.this, EditorActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (item.getItemId() == R.id.item_view_info_links) {
                        //go to insect external links information activity
                        Intent externalLinksIntent = new Intent(context,ExternalLinksActivity.class);
                        externalLinksIntent.putExtra("textWithLinks",myScrollView.getExternalLinksText(bugData));
                        externalLinksIntent.putExtra("insectTitle",textForTitle);
                        externalLinksIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(externalLinksIntent);
                    }

                    return false;
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }

        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    finish();
                }
                return false;
            }
        });

        bugImageCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(images==null)) {

                    Intent intent = new Intent(BugInfoActivity.this, GalleryActivity.class);
                    intent.putExtra("images", images);
                    startActivity(intent);
                }
                else {
                    Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_no_images), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
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

        //getBugData again because images may have been added and with old images array they are not to be loaded in gallery activity
        TaskQueue.prepare().backgroundTask(new Runnable() {
            @Override
            public void run() {
                try {
                    bugDataResponse = Insect.updateBugData(bugId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).guiTask(new Runnable() {
            @Override
            public void run() {
                ResponseCheck.isResponseValid(bugDataResponse,context,fragmentManager);
                System.out.println("NEW BUGDATA: "+bugDataResponse);
            }
        }).subscribeMe();

    }

}
