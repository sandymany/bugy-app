package com.leticija.bugy.auth;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.net.Requester;

import java.io.IOException;
import java.util.Locale;

public class LogInActivity extends AppCompatActivity {

    String TAG = "message";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();

        // SET SERVER IP
        try {
            Requester.setUrl(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ne smije se videti ikona usera i "welcome"
        Group welcomeGroup = findViewById(R.id.groupWelcome);
        welcomeGroup.setVisibility(View.INVISIBLE);

        final TextView textView = findViewById(R.id.textView_message);
        final Button submitButton = findViewById(R.id.button);
        final Button registerButton = findViewById(R.id.button2);
        final EditText usernameText = findViewById(R.id.username_input);
        final EditText passwordText = findViewById(R.id.password_input);
        final ImageView loading = findViewById(R.id.imageView_loading);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    InterfaceFeatures.setButtonEffect(R.color.button_green,R.color.success,submitButton,context);
                }

                String username = usernameText.getText().toString();
                String password = passwordText.getText().toString();

                if (username.equals("") || password.equals("")) {
                    InterfaceFeatures.changeTextViewVisibility(textView,true,context.getResources().getString(R.string.message_fill_everything),R.color.warning);
                }
                else {

                    // setta se animacija za view i posto je po defaultu invisible, postvi se na visible
                    InterfaceFeatures.setRotateAnimation(loading);
                    loading.setVisibility(View.VISIBLE);

                    User.setUsername(username);
                    User.setPassword(password);
                    Authenticator authenticator = new Authenticator(textView, loading);
                    authenticator.login("/login", null, context);

                    System.out.println("LOGGING IN: " + User.getUsername());
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                InterfaceFeatures.setButtonEffect(R.color.button_green,R.color.success,registerButton,context);
                Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

        passwordText.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN){
                    submitButton.callOnClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart!!!");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause!!!");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume!!!");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart!!!!");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy!!!!");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState!!!!");
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState!!!!");
    }
}
