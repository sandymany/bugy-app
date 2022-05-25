package com.leticija.bugy.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.leticija.bugy.ImageAdapter;
import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {

    JSONArray imagesArray;
    Context context;
    ImageAdapter adapter;
    ViewPager viewPager;
    Map<String,String> headersMap = new HashMap<>();
    FragmentManager fragmentManager = getSupportFragmentManager();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_view_activity);

        context = getApplicationContext();

        //first check session cookie
        headersMap.put("Session-cookie", User.sessionCookie);

        String sessionCookieCheckResponse = Requester.request("/home/checkSessionCookie", headersMap, null);

        if (ResponseCheck.isResponseValid(sessionCookieCheckResponse,context,fragmentManager)){
            //create viewPager & override this method to avoid errors while zooming
            viewPager = new ViewPager(this) {

                @Override
                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    try {
                        return super.onInterceptTouchEvent(ev);
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }
            };
            //find LL
            final LinearLayout layoutForViewPager = findViewById(R.id.gallery_layoutForViewPager);

            final TextView countTextView = findViewById(R.id.textView_gallery_count);
            final ImageView loading = findViewById(R.id.loading_gallery);
            InterfaceFeatures.setRotateAnimation(loading);

            TaskQueue.prepare().backgroundTask(new Runnable() {
                @Override
                public void run() {
                    //String imagesString = getIntent().getStringExtra("images");
                    try {
                        JSONObject bugDataObject = new JSONObject(Insect.bugData);
                        System.out.println("BUGDATA OBJECT IN GALLERY: "+bugDataObject);
                        String imagesArrayString = (String) bugDataObject.get("IMAGES");
                        imagesArray = new JSONArray(imagesArrayString);
                        System.out.println("IMAGES ARRAY IN GALLERY: "+imagesArray);
                        System.out.println("IMAGES ARRAY: "+imagesArray);
                        adapter = new ImageAdapter(context, imagesArray, countTextView);

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
                    loading.setVisibility(View.GONE);
                    viewPager.setAdapter(adapter);
                    layoutForViewPager.addView(viewPager);
                    ImageAdapter.setPageCounter(viewPager, countTextView);

                }
            }).subscribeMe();

            //ImageAdapter.setPageCounter(viewPager,countTextView);
            System.out.println("ALL PAGES: " + viewPager.getChildCount());
        }
    }
}