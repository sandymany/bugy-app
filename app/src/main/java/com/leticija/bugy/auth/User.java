package com.leticija.bugy.auth;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;

import com.leticija.bugy.activities.Insect;
import com.leticija.bugy.activities.ResponseCheck;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class User {
    public static String username;
    public static String password;
    public static String sessionCookie;
    public static String userProperties;
    public static String userEmail;


    public static void setUsername(String name) {
        username = name;
    }

    public static void setPassword(String pwd) {
        password = pwd;
    }

    public static void setSessionCookie(String cookie) {
        sessionCookie= cookie;
    }

    public static void setUserProperties(String properties) {
        userProperties = properties;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getSessionCookie() {
        return sessionCookie;
    }

    public static String getUserProperties() {
        return userProperties;
    }

    public static boolean isInsectInCollection (String bugId, String properties) {

        try {

            JSONObject responseObject = new JSONObject(properties);
            JSONArray arrayOfProperties = responseObject.getJSONArray("properties");

            if (arrayOfProperties.length() != 0) {
                JSONObject someObject;
                String bugID;

                for (int i=0; i < arrayOfProperties.length();i++) {
                    someObject = arrayOfProperties.getJSONObject(i);
                    bugID = (String) someObject.get("INSECT_ID");
                    if (bugID.equals(bugId)) {
                        if (someObject.has("PRIVATE_DESCRIPTION")) {
                            Insect.setNotes((String) someObject.get("PRIVATE_DESCRIPTION"));
                        } else {
                            Insect.setNotes("");
                        }
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String requestUserProperties (Context context, FragmentManager fragmentManager, HashMap headers) {

        String response = Requester.request("/home/getProperties",headers,null);

        if (ResponseCheck.isResponseValid(response,context,fragmentManager)) {
            User.userProperties = response;
            return response;
        }
        return null;
    }



    public static void logOut (final FragmentManager fragmentManager, final Context context) {

        TaskQueue.prepare().backgroundTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                Map<String,String> headers = new HashMap<>();
                headers.put("Session-cookie",User.sessionCookie);
                headers.put("All-devices","false");
                String response = Requester.request("/logOut",headers,null);
                ResponseCheck.isResponseValid(response,context,fragmentManager);
            }
        }).subscribeMe();

    }

}
