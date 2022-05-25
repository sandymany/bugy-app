package com.leticija.bugy;

import android.widget.EditText;
import android.widget.Switch;

import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsLoader {

    String userSettings;
    String sessionCookie;
    String username;
    String password;

    public SettingsLoader(String sessionCookie, String username, String password) {
        this.sessionCookie = sessionCookie;
        this.username = username;
        this.password = password;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public void setSessionCookie (String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public void loadCredentials (EditText usernameEdit, EditText pwdEdit) {
        usernameEdit.setText(username);
        pwdEdit.setText(password);
    }

    public void loadNotificationsSettings (String allSettings, final Switch notificationsSwitch, EditText emailInput) {

        try {
            JSONObject requestedObject = new JSONObject(allSettings);
            System.out.println("ALL SETTINGS: "+allSettings);

            String settingsObjectString = (String) requestedObject.get("SETTINGS");
            JSONObject settingsObject = new JSONObject(settingsObjectString);

            User.userEmail = (String) requestedObject.get("EMAIL");
            emailInput.setText(User.userEmail);

            final String notificationsString = (String) settingsObject.get("notifications");

            //set notifications to checked/unchecked depending on settings
            TaskQueue.prepare().guiTask(new Runnable() {
                @Override
                public void run() {

                    if (notificationsString.equals("true")) {
                        notificationsSwitch.setChecked(true);
                    } else {
                        notificationsSwitch.setChecked(false);
                    }
                }
            }).subscribeMe();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
