package com.leticija.bugy.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.activities.HomeActivity;
import com.leticija.bugy.net.Requester;
import java.util.HashMap;
import java.util.Map;

public class Authenticator {

    String response;
    TextView textView;
    Map<String,String> credentials;
    ImageView spinner;

    Authenticator (TextView textView, ImageView spinner) {
        //this.user = user;
        credentials = new HashMap<>();
        credentials.put("Username",User.getUsername());
        credentials.put("Password",User.getPassword());
        this.textView = textView;
        this.spinner = spinner;
    }

    public void login (final String endpoint, final String bodyToSend, final Context context) {

        TaskQueue.subscribe(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                response = Requester.request(endpoint,credentials,bodyToSend);
                User.sessionCookie = response;

                try {
                    if (!(response.equals("false".trim()))) {
                        System.out.println("SESSION COOKIE: " + response);
                        System.out.println("LOGGING YOU IN");

                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                spinner.clearAnimation();
                                spinner.setVisibility(View.INVISIBLE);
                                InterfaceFeatures.changeTextViewVisibility(textView,true,context.getResources().getString(R.string.message_login_successfull),R.color.success);

                                Intent intent = new Intent(context, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        };
                        handler.post(runnable);
                    }
                    else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                spinner.clearAnimation();
                                spinner.setVisibility(View.INVISIBLE);
                                InterfaceFeatures.changeTextViewVisibility(textView, true, context.getResources().getString(R.string.message_wrong_credentials), R.color.warning);
                            }
                        };
                        handler.post(runnable);
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                    Handler handler = new Handler(Looper.getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            spinner.clearAnimation();
                            spinner.setVisibility(View.INVISIBLE);
                            InterfaceFeatures.changeTextViewVisibility(textView,true,context.getResources().getString(R.string.message_connection_error),R.color.warning);
                        }
                    };
                    handler.post(runnable);
                }
            }
        });
    }

    public void register (final String endpoint, final String bodyToSend, final String email, final Context context) {
        TaskQueue.subscribe(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                credentials.put("Email",email);
                response = Requester.request(endpoint,credentials,bodyToSend);
                User.sessionCookie = response;

                try {
                    if (response.equals("false".trim())) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                spinner.clearAnimation();
                                spinner.setVisibility(View.INVISIBLE);
                                InterfaceFeatures.changeTextViewVisibility(textView, true, context.getResources().getString(R.string.message_max_length_of_credentials), R.color.warning);
                            }
                        };
                        handler.post(runnable);
                    }
                    else if (response.equals("true".trim())) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                spinner.clearAnimation();
                                spinner.setVisibility(View.INVISIBLE);
                                InterfaceFeatures.changeTextViewVisibility(textView, true, context.getResources().getString(R.string.message_user_exists), R.color.warning);
                            }
                        };
                        handler.post(runnable);
                    }
                    else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                spinner.clearAnimation();
                                spinner.setVisibility(View.INVISIBLE);
                                InterfaceFeatures.changeTextViewVisibility(textView, true, context.getResources().getString(R.string.message_registration_successfull), R.color.success);

                                Intent intent = new Intent(context, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        };
                        handler.post(runnable);
                    }
                } catch (Exception ex) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            spinner.clearAnimation();
                            spinner.setVisibility(View.INVISIBLE);
                            InterfaceFeatures.changeTextViewVisibility(textView, true, context.getResources().getString(R.string.message_connection_error), R.color.warning);
                        }
                    };
                    handler.post(runnable);
                }
            }
        });
    }
}
