package com.leticija.bugy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leticija.bugy.activities.CollectionActivity;
import com.leticija.bugy.activities.Insect;
import com.leticija.bugy.activities.ResponseCheck;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.auth.LogInActivity;
import com.leticija.bugy.activities.AddBySearch;
import com.leticija.bugy.activities.BugInfoActivity;
import com.leticija.bugy.activities.EditorActivity;
import com.leticija.bugy.net.Requester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceFeatures {
    static int toReturn;

    public static void changeTextViewVisibility (TextView textView, boolean visibility, String text, int color) {
        textView.setText(text);
        if (visibility) {
            textView.setVisibility(View.VISIBLE);
        }
        else {
            textView.setVisibility(View.INVISIBLE);
        }
        textView.setTextColor(ContextCompat.getColor(textView.getContext(),color));
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    public void loadMore (final Context context, final JSONObject jsonObject, final LinearLayout linearLayout, final LayoutInflater inflater, final int brojac, final ImageView loading_image) throws JSONException, IOException {

        final List<ImageView> imagesArray = new ArrayList<>();

        int from = brojac;
        int to;

        try {
            final JSONArray bigArray = jsonObject.getJSONArray("searchResult");
            System.out.println("LENGTH OF JSON: " + bigArray.length());

            if (brojac + 5 < bigArray.length()) {
                to = brojac + 5;
            } else {
                to = bigArray.length();
            }

            AddBySearch.brojac = to;

            for (int i = from; i < to; i++) {

                JSONObject object = (JSONObject) bigArray.get(i);
                ImageView imageView = new ImageView(context);

                //SETTANJE SLIKE NA IMAGEVIEW
                setOptimalImage(imageView,object,context);

                imagesArray.add(imageView);
            }

            for (int i = 0; i < imagesArray.size(); i++) {

                CardView cardView = (CardView) inflater.inflate(R.layout.card_view_template, null);

                InterfaceFeatures.showMoreOnUI(i,loading_image,bigArray,brojac,context, (ArrayList<ImageView>) imagesArray,cardView,linearLayout);
                Thread.sleep(100);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setOptimalImage(ImageView imageView, JSONObject object, Context context) throws JSONException, IOException {
        String imagePath;
        if (object.has("IMAGES")) {
            JSONArray imageArray = new JSONArray(object.get("IMAGES").toString());

            Bitmap bmp = null;
            for(int j = 0; j < imageArray.length(); j++) {
                System.out.println(j);
                imagePath = imageArray.getJSONObject(j).getString("image_name");
                URL url = new URL(context.getString(R.string.base_ip)+"/home/loadImage?imageName="+imagePath);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                if (bmp.getHeight() < bmp.getWidth()) {
                    break;
                }
            }
            if (bmp.getHeight() > bmp.getWidth()) {
                imageView.setRotation((float) 90.0);
            }
            imageView.setImageBitmap(bmp);
        }
        else {
            imageView.setImageResource(R.drawable.blank_image);
        }
    }

    public static Bitmap getOptimalImageBitmap (JSONObject insectObject, Context context) throws JSONException, IOException {

        String imagePath;
        Bitmap bmp = null;
        if (insectObject.has("IMAGES")) {
            JSONArray imageArray = new JSONArray(insectObject.get("IMAGES").toString());

            for(int j = 0; j < imageArray.length(); j++) {
                imagePath = imageArray.getJSONObject(j).getString("image_name");
                URL url = new URL(context.getString(R.string.base_ip)+"/home/loadImage?imageName="+imagePath);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                if (bmp.getHeight() < bmp.getWidth()) {
                    break;
                }
            }

        }
        return bmp;
    }

    public static void setRotateAnimation(View view) {
        RotateAnimation rotateAnimation = new RotateAnimation(0,1440, Animation.RELATIVE_TO_SELF,
                .5f,Animation.RELATIVE_TO_SELF,.5f);
        rotateAnimation.setDuration(8000);
        System.out.println("START: "+rotateAnimation.getStartOffset());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(rotateAnimation);

    }

    public static void sessionCookieDialogue (final Context context, FragmentManager fragmentManager) {

        Runnable loginRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        };

        Runnable exitAppRunnable = new Runnable() {
            @Override
            public void run() {
                return;

            }
        };

        DialogCreator dialogCreator = new DialogCreator(context.getResources().getString(R.string.dialog_session_expired_title),context.getResources().getString(R.string.dialog_session_expired),"",context.getResources().getString(R.string.runnable_login),loginRunnable,exitAppRunnable);
        dialogCreator.setCancelable(false);
        dialogCreator.show(fragmentManager,"example dialog");

    }

    public static void sureUWantToDeleteAccountDialog (final Context context, final FragmentManager fragmentManager) {

        Runnable noRunnable = new Runnable() {
            @Override
            public void run() {
                return;
            }
        };

        Runnable yesRunnable = new Runnable() {
            @Override
            public void run() {

                // send request for deleting account and go to login activity
                TaskQueue.prepare().backgroundTask(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {

                        Map<String,String> headers = new HashMap<>();
                        headers.put("Session-cookie", User.sessionCookie);
                        String response = Requester.request("/home/deleteAccount",headers,null);
                        ResponseCheck.isResponseValid(response,context,fragmentManager);
                    }
                }).guiTask(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context,LogInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                }).subscribeMe();
            }
        };

        DialogCreator dialogCreator = new DialogCreator("!",context.getResources().getString(R.string.dialog_delete_account),
                context.getResources().getString(R.string.runnable_no),context.getResources().getString(R.string.runnable_yes),yesRunnable,noRunnable);
        dialogCreator.setCancelable(false);
        dialogCreator.show(fragmentManager,"example dialog");

    }

    public static void insectExistsInDatabase (final Context context, final FragmentManager fragmentManager) {

        Runnable searchRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                Intent intent = new Intent(context,AddBySearch.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        };

        Runnable nothingRunnable = new Runnable() {
            @Override
            public void run() {
                return;
            }
        };

        DialogCreator dialogCreator = new DialogCreator(context.getResources().getString(R.string.dialog_insect_exists_title),
                context.getResources().getString(R.string.dialog_insect_exists),context.getResources().getString(R.string.runnable_cancel),context.getResources().getString(R.string.runnable_go_to_search),searchRunnable,nothingRunnable);
        dialogCreator.show(fragmentManager,"example dialog");

    }

    public static void addInsectToCollectionDialogue (final String bugId, final Context context, final FragmentManager fragmentManager) {

        Runnable addInsectRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                Insect.addInsectToCollection(bugId,fragmentManager,context);
            }
        };

        Runnable dontAddRunnable = new Runnable() {
            @Override
            public void run() {
                return;
            }
        };

        DialogCreator dialogCreator = new DialogCreator("",context.getResources().getString(R.string.dialog_add_to_collection),
                context.getResources().getString(R.string.runnable_no),context.getResources().getString(R.string.runnable_yes),addInsectRunnable,dontAddRunnable);
        dialogCreator.show(fragmentManager,"example dialog");

    }

    public static void serverErrorDialogue (final Context context, FragmentManager fragmentManager) {
        Runnable loginRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        };

        Runnable exitAppRunnable = new Runnable() {
            @Override
            public void run() {
                return;
            }
        };

        DialogCreator dialogCreator = new DialogCreator(context.getResources().getString(R.string.dialog_server_error_title),
                context.getResources().getString(R.string.dialog_server_error),"",context.getResources().getString(R.string.runnable_login),loginRunnable,exitAppRunnable);
        dialogCreator.setCancelable(false);
        dialogCreator.show(fragmentManager,"example dialog");

    }

    public static void credentialsUpdatedSuccessfully (final Context context, FragmentManager fragmentManager) {

        Runnable nothing = new Runnable() {
            @Override
            public void run() {
                return;
            }
        };

        Runnable loginRunnable= new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context,LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        };

        DialogCreator dialogCreator = new DialogCreator("",context.getResources().getString(R.string.dialog_credentials_updated),
                "",context.getResources().getString(R.string.runnable_login),loginRunnable,nothing);
        dialogCreator.setCancelable(false);
        dialogCreator.show(fragmentManager,"example dialog");

    }

    public static void showMoreOnUI(int i, final ImageView loading_image, final JSONArray bigArray,final int brojac, final Context context, final ArrayList<ImageView> imagesArray, final CardView cardView, final LinearLayout linearLayout) {

        Handler handler = new Handler(Looper.getMainLooper());
        final int finalI = i; //da bi mogla pristupati indeksima, mora biti final
        Runnable runnable = new Runnable() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void run() {

                loading_image.clearAnimation();
                loading_image.setVisibility(View.INVISIBLE);

                try {
                    final JSONObject object = (JSONObject) bigArray.get(brojac+finalI);

                    LinearLayout layout = new LinearLayout(context);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layout.setLayoutParams(layoutParams);
                    cardView.addView(layout);

                    final ImageView imageView = imagesArray.get(finalI);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(250,200);
                    imageParams.leftMargin = 10;
                    imageParams.topMargin = 10;
                    imageParams.bottomMargin = 10;
                    imageView.setLayoutParams(imageParams);
                    layout.addView(imageView);

                    View viewDivider = new View(context);
                    viewDivider.setBackgroundColor(context.getResources().getColor(R.color.gray));
                    viewDivider.setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams dividerparams = new LinearLayout.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
                    dividerparams.leftMargin = 10;
                    dividerparams.topMargin = 10;
                    dividerparams.bottomMargin = 10;
                    viewDivider.setLayoutParams(dividerparams);
                    layout.addView(viewDivider);

                    String textForView;
                    if (object.has("NAME")) {
                        textForView = (brojac + finalI + 1) + ". " + object.get("NAME").toString();
                    } else {
                        textForView = (brojac+finalI+1) + ". "+object.get("SPECIES").toString();
                    }
                    TextView textView = new TextView(context);
                    textView.setText(textForView);
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                    LinearLayout.LayoutParams textparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textparams.leftMargin = 10;
                    textparams.rightMargin = 5;
                    textparams.gravity = Gravity.CENTER;
                    textView.setLayoutParams(textparams);
                    layout.addView(textView);

                    InterfaceFeatures.fadeIn(cardView,400);
                    linearLayout.addView(cardView);
                    //InterfaceFeatures.fadeIn(cardView,300);



                    cardView.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View v) {
                            cardView.setCardBackgroundColor(context.getColor(R.color.green));

                            Intent intent = new Intent(context, BugInfoActivity.class);
                            intent.putExtra("data", object.toString());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //intent.putExtra("imageURL", urlForImage);
                            context.startActivity(intent);

                            TaskQueue.subscribe(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    cardView.setCardBackgroundColor(context.getColor(R.color.white));
                                }
                            });

                        }
                    });

                    cardView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            cardView.setOnClickListener(null);
                            fadeOut(cardView,600);
                            return false;
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        handler.post(runnable);
    }

    public static void loadMoreCollectionToScrollView (JSONArray objectsArray, int brojac, LinearLayout scrollLayout, Context context, LayoutInflater inflater, ImageView loading) throws JSONException, IOException {

        int from = brojac;
        int to;

        if (brojac + 5 < objectsArray.length()) {
            to = brojac + 5;
        } else {
            to = objectsArray.length();
        }

        for (int i = from; i < to; i++) {

            JSONObject objectToLoad = objectsArray.getJSONObject(i);
            loadCollectionObjectToScrollView(objectToLoad,scrollLayout,context,inflater, i, loading);

        }

        CollectionActivity.brojac = to;

    }

    private static void loadCollectionObjectToScrollView (final JSONObject insectObject, final LinearLayout scrollLayout, final Context context, final LayoutInflater inflater, final int redniBroj, final ImageView loading) throws JSONException, IOException {

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                //loading.clearAnimation();
                //loading.setVisibility(View.INVISIBLE);

                final CardView cardView = (CardView) inflater.inflate(R.layout.card_view_template, null);

                LinearLayout layout = new LinearLayout(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layout.setLayoutParams(layoutParams);
                cardView.addView(layout);

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = getOptimalImageBitmap(insectObject,context);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageView imageView = new ImageView(context);
                imageView.setImageBitmap(imageBitmap);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(250,200);
                imageParams.leftMargin = 10;
                imageParams.topMargin = 10;
                imageParams.bottomMargin = 10;
                imageView.setLayoutParams(imageParams);
                layout.addView(imageView);

                View viewDivider = new View(context);
                viewDivider.setBackgroundColor(context.getResources().getColor(R.color.gray));
                viewDivider.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams dividerparams = new LinearLayout.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
                dividerparams.leftMargin = 10;
                dividerparams.topMargin = 10;
                dividerparams.bottomMargin = 10;
                viewDivider.setLayoutParams(dividerparams);
                layout.addView(viewDivider);

                String textForView = "INSECT NAME";

                if (insectObject.has("NAME")) {
                    try {
                        String name = (String) insectObject.get("NAME");
                        if (!name.trim().equals("")) {
                            textForView = (redniBroj + 1) + ". " + insectObject.get("NAME").toString();
                        } else {
                            textForView = (redniBroj+1) + ". "+insectObject.get("GENUS")+" "+insectObject.get("SPECIES").toString();
                            System.out.println("IM SETTING"+textForView);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                TextView textView = new TextView(context);
                textView.setText(textForView);
                textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                LinearLayout.LayoutParams textparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textparams.leftMargin = 10;
                textparams.rightMargin = 5;
                textparams.gravity = Gravity.CENTER;
                textView.setLayoutParams(textparams);
                layout.addView(textView);

                InterfaceFeatures.fadeIn(cardView,400);
                scrollLayout.addView(cardView);

                cardView.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        cardView.setCardBackgroundColor(context.getColor(R.color.green));

                        Intent intent = new Intent(context, BugInfoActivity.class);
                        intent.putExtra("data", insectObject.toString());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.putExtra("imageURL", urlForImage);
                        context.startActivity(intent);

                        TaskQueue.subscribe(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                cardView.setCardBackgroundColor(context.getColor(R.color.white));
                            }
                        });
                    }
                });
            }
        };
        handler.post(runnable);

    }

    public static void showKeyboard (View view,boolean bool, Context context) {

        if (bool == false) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public static void addButtonEffect (final Context context, final View button, final int newDrawable, final int oldDrawable, final int time) {

        TaskQueue.prepare().guiTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                button.setBackground(context.getResources().getDrawable(newDrawable));
            }
        }).backgroundTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).guiTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                button.setBackground(context.getResources().getDrawable(oldDrawable));
            }
        }).subscribeMe();

    }

    public static void credentialsChangeDialog(final Context context, final FragmentManager fragmentManager, final ImageView loading, final TextView messageText, final SettingsUpdater settings) {

        Runnable yesRunnable = new Runnable() {
            @Override
            public void run() {
                settings.updateCredentialsChanges(fragmentManager,context,loading,messageText);

            }
        };

        Runnable noRunnable = new Runnable() {
            @Override
            public void run() {
                return;
            }
        };

        DialogCreator dialogCreator = new DialogCreator("",context.getResources().getString(R.string.dialog_credentials_change),
                context.getResources().getString(R.string.runnable_yes),context.getResources().getString(R.string.runnable_no),noRunnable,yesRunnable);
        dialogCreator.setCancelable(false);
        dialogCreator.show(fragmentManager,"example dialog");

    }

    public static void fadeOut (final View view,int duration) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(duration);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                view.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        view.startAnimation(fadeOut);
    }

    public static void fadeIn (final View view,int duration) {

        Animation fadeOut = new AlphaAnimation(0,1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(duration);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                view.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        view.startAnimation(fadeOut);
    }

    public static void fadeInAndOut (final View view, final int durationBetweenFadeInAndOut) {

        TaskQueue.prepare().guiTask(new Runnable() {
            @Override
            public void run() {
                fadeIn(view,500);
            }
        }).backgroundTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(durationBetweenFadeInAndOut);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).guiTask(new Runnable() {
            @Override
            public void run() {

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(500);

                fadeOut.setAnimationListener(new Animation.AnimationListener()
                {
                    public void onAnimationEnd(Animation animation)
                    {
                        view.setVisibility(View.INVISIBLE);
                    }
                    public void onAnimationRepeat(Animation animation) {}
                    public void onAnimationStart(Animation animation) {}
                });

                view.startAnimation(fadeOut);
            }
        }).subscribeMe();

    }

    public static void expandOrCollapseView (final View view) {

        TaskQueue.prepare().guiTask(new Runnable() {
            @Override
            public void run() {
                if (view.getVisibility() == View.GONE) {
                    System.out.println("expanding view!");
                    InterfaceFeatures.fadeIn(view,500);
                    view.setVisibility(View.VISIBLE);
                }
                else {
                    InterfaceFeatures.fadeOut(view,500);
                    view.setVisibility(View.GONE);
                }
            }
        }).subscribeMe();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setButtonEffect(final int firstColor, final int toColor, final Button button, final Context context) {

        button.setBackgroundResource(toColor);

        TaskQueue.subscribe(new Runnable() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                TaskQueue.prepare().guiTask(new Runnable() {
                    @Override
                    public void run() {
                        button.setBackgroundResource(firstColor);
                    }
                }).subscribeMe();
            }
        });

    }

}
