package com.leticija.bugy;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leticija.bugy.activities.ResponseCheck;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import java.util.HashMap;
import java.util.Map;

public class SettingsUpdater {

    String newUsername;
    String newPassword;
    String newEmail;


    public void setNewUsername (String username) {
        newUsername = username;
    }

    public void setNewPassword (String password) {
        newPassword = password;
    }

    public void setNewEmail (String email) {

        this.newEmail = email;
    }

    public void updateCredentialsChanges(final FragmentManager fragmentManager, final Context context, final ImageView loading, final TextView messageTextView) {

        if (newUsername.trim().equals("") || newPassword.trim().equals("") || newEmail.trim().equals("")) {
            TaskQueue.prepare().guiTask(new Runnable() {
                @Override
                public void run() {

                    InterfaceFeatures.changeTextViewVisibility(messageTextView,true,"FILL EVERYTHING, PLEASE.",R.color.warning);

                }
            }).subscribeMe();
        }

        else if (!newUsername.equals(User.username) || !newPassword.equals(User.password) || !newEmail.equals(User.userEmail)) {

            TaskQueue.prepare().guiTask(new Runnable() {
                @Override
                public void run() {

                    InterfaceFeatures.setRotateAnimation(loading);

                }
            }).backgroundTask(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("New-username",newUsername.trim());
                    headers.put("New-password",newPassword.trim());
                    headers.put("New-email",newEmail.trim());
                    headers.put("Session-cookie",User.sessionCookie);
                    String response = Requester.request("/home/updateCredentials",headers,null);
                    if (ResponseCheck.isResponseValid(response,context,fragmentManager)) {
                        assert response != null;
                        if (response.equals("true")) {
                            InterfaceFeatures.credentialsUpdatedSuccessfully(context,fragmentManager);
                        } else {
                            TaskQueue.prepare().guiTask(new Runnable() {
                                @Override
                                public void run() {
                                    messageTextView.setText(context.getResources().getString(R.string.message_user_exists));
                                    InterfaceFeatures.fadeInAndOut(messageTextView,2000);
                                }
                            }).subscribeMe();
                        }
                    }

                }
            }).guiTask(new Runnable() {
                @Override
                public void run() {
                    loading.clearAnimation();
                    loading.setVisibility(View.INVISIBLE);
                }
            }).subscribeMe();

        } else {
            TaskQueue.prepare().guiTask(new Runnable() {
                @Override
                public void run() {
                    messageTextView.setText(context.getResources().getString(R.string.message_nothing_changed));
                    InterfaceFeatures.fadeInAndOut(messageTextView,2000);
                    //InterfaceFeatures.changeTextViewVisibility(messageTextView,true,"Nothing changed.",R.color.dark_green_text);
                }
            }).subscribeMe();
        }
    }

}
