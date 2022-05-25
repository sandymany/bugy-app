package com.leticija.bugy.activities;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;

import com.leticija.bugy.auth.User;
import com.leticija.bugy.net.Requester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Insect {

    public static String bugData;
    public static String publicDescription;
    public static String notes;
    public static String insectTitle;
    public static String bugId;

    public static void setInsectTitle (String someInsectTitle) {
        insectTitle = someInsectTitle;
    }

    public static void setBugData (String someBugData) {
        bugData = someBugData;
    }

    public static void setPublicDescription (String somePublicDescription) {
        publicDescription = somePublicDescription;
    }

    public static void setBugId (String bugID) {
        bugId = bugID;
    }

    public static void setNotes (String someNotes) {
        notes = someNotes;
    }

    public static String getBugData () {
        return bugData;
    }

    public static String getPublicDescription () {
        return publicDescription;
    }

    public static String getNotes() {
        return notes;
    }

    public static String getInsectTitle () {
        return insectTitle;
    }

    public static String getBugId() {
        return bugId;
    }

    public static void clearCache () {

        bugData = null;
        publicDescription = null;
        notes = null;
        insectTitle = null;

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String updateBugData (String bugId) throws JSONException {
        Map<String,String> headers = new HashMap<>();
        headers.put("Session-cookie", User.sessionCookie);
        headers.put("Insect-id",Insect.bugId);
        String response = Requester.request("/home/getBugData",headers,null);
        JSONObject responseObject = new JSONObject(response);
        JSONArray arrayInObject = responseObject.getJSONArray("bugData");
        JSONObject bugDataObject = arrayInObject.getJSONObject(0);
        bugData = bugDataObject.toString();

        return bugData;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void addInsectToCollection (String bugId, FragmentManager fragmentManager, Context context) {

        Map<String,String> headers = new HashMap<>();
        headers.put("Session-cookie",User.sessionCookie);
        headers.put("Insect-id",bugId);
        String response = Requester.request("/home/addToCollection",headers,null);
        ResponseCheck.isResponseValid(response,context,fragmentManager);

    }

}
