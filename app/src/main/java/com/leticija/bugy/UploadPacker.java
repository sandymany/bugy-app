package com.leticija.bugy;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Base64;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.leticija.bugy.activities.ResponseCheck;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadPacker {

    List<TextView> classGroups;
    List<AutoCompleteTextView> classGroupNames;
    String publicDescription;
    String notes;
    String sessionCookie;
    Bitmap bitmap;

    public UploadPacker(String sc, List<TextView> someClassGroups, List<AutoCompleteTextView> someClassGroupNames, String somePublicDescription, String someNotes) {
        this.sessionCookie = sc;
        this.classGroups = someClassGroups;
        this.classGroupNames = someClassGroupNames;
        this.publicDescription = somePublicDescription;
        this.notes = someNotes;
    }

    public void setClassGroups(List<TextView> someClassGroups) {
        this.classGroups = someClassGroups;
    }

    public void setClassGroupNames(List<AutoCompleteTextView> someClassGroupNames) {
        this.classGroupNames = someClassGroupNames;
    }

    public void setPublicDescription (String somePublicDescription) {
        this.publicDescription = somePublicDescription;
    }

    public void setNotes(String someNotes) {
        this.notes = someNotes;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String packAndUploadNewInsect(final Context context, FragmentManager fragmentManager) {

        Map<String,String> headers;
        String jsonToSend = getPackedJSON();
        try {
            JSONObject object = new JSONObject(jsonToSend);
            JSONObject classificationObject = object.getJSONObject("classification");
            if (!(classificationObject.has("GENUS") & classificationObject.has("SPECIES"))) {
                TaskQueue.prepare().guiTask(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"Please enter GENUS and SPECIES!",Toast.LENGTH_LONG).show();
                    }
                }).subscribeMe();
            } else { //šalje se zahtjev serveru da doda novog kukca.

                byte [] bytes = jsonToSend.getBytes();
                headers = new HashMap<>();
                headers.put("Session-cookie", User.sessionCookie);
                headers.put("Body-length",String.valueOf(bytes.length));

                String response = Requester.request("/home/uploadNewInsect",headers,jsonToSend);
                if (ResponseCheck.isResponseValid(response,context,fragmentManager)) {
                    //returns server response (id server gave to new insect in database.)
                    return response;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void packAndUploadTextualParts(Context context, FragmentManager fragmentManager, String bugId) {

        Map<String,String> headers;
        String response;

        String jsonToSend = getPackedJSON();

        headers = new HashMap<>();
        headers.put("Insect-id",bugId);
        headers.put("Session-cookie", User.sessionCookie);
        byte[] byteArray = jsonToSend.getBytes();
        int length = byteArray.length;
        headers.put("Body-length",String.valueOf(length));

        response = Requester.request("/home/uploadTextualChanges",headers,jsonToSend);
        ResponseCheck.isResponseValid(response,context,fragmentManager);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void packAndUploadPhoto(Bitmap photoBitmap, String insectId) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Map<String,String> headers = new HashMap<>();

        //ili b.size!!!!
        headers.put("Body-length",String.valueOf(imageEncoded.length()));
        headers.put("Session-cookie",User.sessionCookie);
        headers.put("Insect-id", insectId);

        System.out.println("SENDING IMAGE TO SERVER !");
        String response = Requester.request("/home/uploadPhotos", headers, imageEncoded);
        System.out.println(response);
    }

    public String getPackedJSON () {

        JSONObject jsonToSend = new JSONObject();

        try {
            //pack classGroups and classGroupNames together to smaller JSONObject
            JSONObject classGroupsObject = new JSONObject();
            for (int i = 0; i < classGroups.size(); i++) {
                if (!(classGroupNames.get(i).getText().toString().trim().equals(""))) {
                    classGroupsObject.put((String) classGroups.get(i).getText(), classGroupNames.get(i).getText().toString().trim());
                }
            }

            jsonToSend.put("classification", classGroupsObject);
            jsonToSend.put("private_description", notes);
            jsonToSend.put("public_description", publicDescription);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonToSend);
        return (jsonToSend.toString());
    }

}
