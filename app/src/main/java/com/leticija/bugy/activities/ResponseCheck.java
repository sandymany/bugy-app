package com.leticija.bugy.activities;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.auth.User;

public class ResponseCheck{

    public static boolean isResponseValid (String response, Context context, FragmentManager fragmentManager) {

        if (response == null) {
            InterfaceFeatures.serverErrorDialogue(context,fragmentManager);
            return false;
        }
        else if (response.trim().equals("false")) {
            InterfaceFeatures.sessionCookieDialogue(context,fragmentManager);
            return false;
        }
        return true;
    }

    public static void setVariable (String endpoint, String response) {

        if (endpoint.equals("/home/getProperties")) {
            System.out.println("SETTING VARIABLE USER PROPERTIES..");
            User.userProperties = response;
        }
    }
}
