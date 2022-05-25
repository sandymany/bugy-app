package com.leticija.bugy.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.net.Requester;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AddBySearch extends AppCompatActivity {

    String response;
    public static int brojac;
    static JSONObject jsonObject;
    static LinearLayout linearLayout;
    static LayoutInflater inflater;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_addbugs_search);

        final ImageView loading = findViewById(R.id.loading_search);
        final TextView searchMessage = findViewById(R.id.textView_searchMessage);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Context context = getApplicationContext();
        //FIND EVERYTHING YOU NEED
        final EditText searchInput = findViewById(R.id.searchBugs_editText);
        final Button searchButton = findViewById(R.id.search_button);

        // BUTTON FUNCTIONALITIES
        searchButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                InterfaceFeatures.addButtonEffect(context,searchButton,R.drawable.search_clicked,R.drawable.search,200);
                final ScrollView scrollView = findViewById(R.id.search_scrollView);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                // mora se maknuti tipkovnica
                View view = getCurrentFocus();
                try {
                    InterfaceFeatures.showKeyboard(view, false, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                searchMessage.setVisibility(View.INVISIBLE);

                if (!(searchInput.getText().toString().trim().isEmpty())) {

                    System.out.println("SEARCHING: " + searchInput.getText().toString());
                    HomeActivity.headersMap.put("To-search", searchInput.getText().toString());

                    // LOADING SPINNER JE VIDLJIV
                    loading.setVisibility(View.VISIBLE);
                    InterfaceFeatures.setRotateAnimation(loading);

                    linearLayout = findViewById(R.id.search_linearLayout);
                    linearLayout.removeAllViews(); // da se ne dodaje novo kaj searcha na kraj nego se refresha celi view !

                    // U POZADINI SE ŠALJE REQ SERVERU ZA JSON
                    Thread t = new Thread() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        public void run() {
                            response = Requester.request("/home/searchBugs",HomeActivity.headersMap,null);
                            System.out.println("RESPONSE: "+response);
                            if (!(response.equals("false"))) {
                                try {
                                    jsonObject = new JSONObject(response);
                                    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                                    final int brojNadenih = jsonObject.getJSONArray("searchResult").length();

                                    if (brojNadenih != 0) {

                                        Handler handler = new Handler(Looper.getMainLooper());
                                        Runnable runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                searchMessage.setVisibility(View.VISIBLE);
                                                searchMessage.setText(context.getResources().getString(R.string.number_of_results)+brojNadenih);
                                                InterfaceFeatures.fadeIn(searchMessage,1000);
                                                //InterfaceFeatures.fadeOut(searchMessage,1000);
                                                loading.clearAnimation();
                                                loading.setVisibility(View.INVISIBLE);
                                            }
                                        };
                                        handler.post(runnable);

                                        InterfaceFeatures loadmore = new InterfaceFeatures();
                                        loadmore.loadMore(context, jsonObject, linearLayout, inflater, 0, loading);

                                        System.out.println("PRVI BROJAC JE: " + brojac);

                                    }
                                    else {
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        Runnable runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("NO BUGS!");
                                                InterfaceFeatures.fadeIn(searchMessage,1000);
                                                searchMessage.setText(context.getResources().getString(R.string.no_results));
                                                //InterfaceFeatures.fadeOut(searchMessage,1000);
                                                loading.clearAnimation();
                                                loading.setVisibility(View.INVISIBLE);
                                            }
                                        };
                                        handler.post(runnable);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Handler handler = new Handler(Looper.getMainLooper());
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        InterfaceFeatures.sessionCookieDialogue(context,fragmentManager);
                                        loading.clearAnimation();
                                        loading.setVisibility(View.INVISIBLE);
                                    }
                                };
                                handler.post(runnable);
                            }

                        }
                    };
                    t.start();

                }

                scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                        if (scrollView.getChildAt(0).getBottom() == (scrollView.getHeight() + scrollView.getScrollY())) {

                            System.out.println("OOPS, BOTTOM!!, LOADING MORE !");
                            //InterfaceFeatures interfaceFeatures = new InterfaceFeatures();
                            try {
                                if (brojac != jsonObject.getJSONArray("searchResult").length()) {
                                    System.out.println("LEN: "+(jsonObject.getJSONArray("searchResult").length()));
                                    loading.setVisibility(View.VISIBLE);
                                    InterfaceFeatures.setRotateAnimation(loading);

                                    TaskQueue.subscribe(new Runnable() {
                                        public void run(){
                                            InterfaceFeatures interfaceFeatures = new InterfaceFeatures();
                                            try {
                                                interfaceFeatures.loadMore(context, jsonObject, linearLayout, inflater, brojac, loading);
                                                //brojac = interfaceFeatures.loadMore(context, jsonObject, linearLayout, inflater, brojac, loading);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
            }
        });

        searchInput.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN){
                    searchButton.callOnClick();
                    return true;
                }
                return false;
            }
        });


    }

}