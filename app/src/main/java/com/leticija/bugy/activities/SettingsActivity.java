package com.leticija.bugy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.SettingsLoader;
import com.leticija.bugy.SettingsUpdater;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    String response;
    Map<String,String> header;
    Switch notificationSwitch;
    Context context;
    FragmentManager fragmentManager;
    Button submitCredentialsButton;
    Button backButton;
    ImageView loading;
    TextView messageText;
    Button logOutFromAllDevicesButton;
    TextView messageLogOut;
    SettingsLoader settingsLoader;
    Button deleteAccountButton;
    ImageView logoImage;
    EditText emailEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_settings);

        context = getApplicationContext();
        fragmentManager = getSupportFragmentManager();


        //find what yout need
        emailEdit = findViewById(R.id.email_input_settings);
        logoImage = findViewById(R.id.logo_image);
        deleteAccountButton = findViewById(R.id.delete_account_button);
        messageLogOut = findViewById(R.id.message2_logOutFromDevices_settings);
        logOutFromAllDevicesButton = findViewById(R.id.log_from_all_devices_button);
        messageText = findViewById(R.id.message_settings);
        loading = findViewById(R.id.loading_settings);
        final EditText usernameInput = findViewById(R.id.usernameEditText_settings);
        final EditText passwordInput = findViewById(R.id.passwordEditText_settings);
        notificationSwitch = findViewById(R.id.switchNotifications_settings);
        backButton = findViewById(R.id.back_button_settings);
        submitCredentialsButton = findViewById(R.id.submit_credentials_settings);
        //set username on toolbar
        TextView homeText = findViewById(R.id.text_welcome);
        InterfaceFeatures.changeTextViewVisibility(homeText,true, User.username,R.color.dark_green_text);


        //SETUP SETTINGS LAYOUT (usrname, pwd, notifications settings (on/off)):
        settingsLoader = new SettingsLoader(User.sessionCookie,User.username,User.password);
        settingsLoader.loadCredentials(usernameInput,passwordInput);
        //REQUEST USER SETTINGS
        TaskQueue.prepare().backgroundTask(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                Map<String,String> headers = new HashMap<>();
                headers.put("Session-cookie",User.sessionCookie);
                response = Requester.request("/home/getSettings",headers,null);
                ResponseCheck.isResponseValid(response,context,fragmentManager);
            }
        }).guiTask(new Runnable() {
            @Override
            public void run() {
                settingsLoader.loadNotificationsSettings(response, notificationSwitch, emailEdit);
            }
        }).subscribeMe();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        logOutFromAllDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                InterfaceFeatures.setRotateAnimation(loading);

                TaskQueue.prepare().backgroundTask(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {

                        header = new HashMap<>();
                        header.put("Session-cookie",User.sessionCookie);
                        header.put("All-devices","true");
                        String response=Requester.request("/logOut",header,null);
                        if (ResponseCheck.isResponseValid(response,context,fragmentManager)) {

                            TaskQueue.prepare().guiTask(new Runnable() {
                                @Override
                                public void run() {
                                    InterfaceFeatures.changeTextViewVisibility(messageLogOut,false,context.getResources().getString(R.string.logged_out_from_all),R.color.success);
                                    InterfaceFeatures.fadeInAndOut(messageLogOut,2000);
                                }
                            }).subscribeMe();
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
        });

        submitCredentialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SettingsUpdater settingsUpdater = new SettingsUpdater();
                settingsUpdater.setNewUsername(usernameInput.getText().toString());
                settingsUpdater.setNewPassword(passwordInput.getText().toString());
                settingsUpdater.setNewEmail(emailEdit.getText().toString());
                System.out.println("NEW EMAIL: "+emailEdit.getText().toString());


                InterfaceFeatures.credentialsChangeDialog(context,fragmentManager,loading,messageText,settingsUpdater);

            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InterfaceFeatures.sureUWantToDeleteAccountDialog(context,fragmentManager);

            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                final Map<String,String> headersMap = new HashMap<>();
                headersMap.put("Session-cookie",User.sessionCookie);
                final String notificationsString;

                if (isChecked) {
                    //send request to server that you want to change notification setting to true
                    notificationsString = "true";
                } else {
                    //send reg to srvr that you want to change notifications setting to false
                    notificationsString = "false";
                }
                //send changes to server
                TaskQueue.prepare().backgroundTask(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        headersMap.put("Notifications",notificationsString);
                        String response = Requester.request("/home/changeNotificationsSettings",headersMap,null);
                        ResponseCheck.isResponseValid(response,context,fragmentManager);
                    }
                }).subscribeMe();

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
