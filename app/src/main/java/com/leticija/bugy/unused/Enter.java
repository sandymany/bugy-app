package com.leticija.bugy.unused;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.TextView;

import com.leticija.bugy.R;
import com.leticija.bugy.InterfaceFeatures;

import java.util.HashMap;
import java.util.Map;

public class Enter {

    public static String sessionCookie;
    public static String username;
    public static String password;
    public static Map<String,String> headersMap;
    Context context;

    Enter (String username, String password,Context context) {
        this.username = username;
        this.password = password;
        this.context = context;
        headersMap = new HashMap<>();
        headersMap.put("Username",username);headersMap.put("Password",password);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String logIn (TextView textView) throws NullPointerException{

        //sessionCookie = Requester.wrapInThread("/login",headersMap,null);
        try {
            if (!(sessionCookie.equals("false".trim()))) {
                System.out.println("SESSION COOKIE: " + sessionCookie);
                System.out.println("LOGGING YOU IN");
                InterfaceFeatures.changeTextViewVisibility(textView, true, "Login successful!", R.color.success);
                return (sessionCookie);
            }
            System.out.println("####### WRONG ########");
            InterfaceFeatures.changeTextViewVisibility(textView, true, "Invalid credentials.\nTry to register first.", R.color.warning);
            //return ("false");
            return (null);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            InterfaceFeatures.changeTextViewVisibility(textView,true,"Sorry\nConnection is down",R.color.warning);
        }
        return (null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String register (TextView textView) {
        //sessionCookie = Requester.wrapInThread("/register",headersMap,null);
        System.out.println("SessionCookie: "+sessionCookie);
        try {
            if (sessionCookie.equals("false".trim())) {
                InterfaceFeatures.changeTextViewVisibility(textView, true, "Credentials should\ncontain max 15 characters", R.color.warning);
                //return (sessionCookie);
                return (null);
            }
            else if (sessionCookie.equals("true".trim())) {
                InterfaceFeatures.changeTextViewVisibility(textView,true,"User already exists",R.color.warning);
                //return (sessionCookie);
                return (null);
            }
            System.out.println("REGISTRATION SUCCESSFUL! : " + sessionCookie);
            InterfaceFeatures.changeTextViewVisibility(textView, true, "Registration successful!", R.color.success);
            return (sessionCookie);

        } catch (Exception ex) {
            InterfaceFeatures.changeTextViewVisibility(textView,true,"Sorry\nConnection is down",R.color.warning);
            ex.printStackTrace();
        }
        return (null);
    }



}
