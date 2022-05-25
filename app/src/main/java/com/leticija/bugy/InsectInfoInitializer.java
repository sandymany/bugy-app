package com.leticija.bugy;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.activities.Insect;
import com.leticija.bugy.activities.ResponseCheck;
import com.leticija.bugy.net.Requester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class InsectInfoInitializer {

    ScrollView scrollView;
    Context context;
    JSONObject responseJSON;
    JSONArray responseArray;
    public static TextView infoWithLinks;

    public InsectInfoInitializer(ScrollView scrollView, Context context) {
        this.scrollView = scrollView;
        this.context = context;
    }

    public void setTitle (String textForTitle, TextView textView) {
        textView.setText(textForTitle);
    }

    public void setImage (final JSONObject bugData, final ImageView loading, final ImageView bugImage) {

        InterfaceFeatures.setRotateAnimation(loading);
        loading.setVisibility(View.VISIBLE);

        //sljedece se odvija u odvojenom threadu zbog requestanja slike
        TaskQueue.prepare().backgroundTask(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bmp = InterfaceFeatures.getOptimalImageBitmap(bugData,context);

                    if (bmp != null) {
                        TaskQueue.prepare().guiTask(new Runnable() {
                            @Override
                            public void run() {
                                bugImage.setImageBitmap(bmp);
                                loading.clearAnimation();
                                loading.setVisibility(View.INVISIBLE);
                                InterfaceFeatures.fadeIn(scrollView,500);

                            }
                        }).subscribeMe();
                    }
                    else {
                        TaskQueue.prepare().guiTask(new Runnable() {
                            @Override
                            public void run() {
                                bugImage.setImageResource(R.drawable.blank_image);
                                loading.clearAnimation();
                                loading.setVisibility(View.INVISIBLE);
                                InterfaceFeatures.fadeIn(scrollView,500);

                            }
                        }).subscribeMe();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeMe();

    }

    public void loadInsectClassification (final JSONObject bugData, LinearLayout scrollLayout) throws JSONException {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        LinearLayout relativeLayout = (LinearLayout) inflater.inflate(R.layout.template_bugdata, null);

        TextView titleToExpand = relativeLayout.findViewById(R.id.bugData_subtitle);
        titleToExpand.setText(context.getResources().getString(R.string.subtitle_classification));

        final TextView textToShow = relativeLayout.findViewById(R.id.content_text);
        String classificationText = "DOMAIN: <i>Eukarya</i><br>KINGDOM: <i>Animalia</i><br>";

        ArrayList<String> classList = new ArrayList<>(Arrays.asList("PHYLUM", "SUBPHYLUM",
                "CLASS", "ORDER", "SUBORDER", "INFRAORDER", "SUPERFAMILY", "FAMILY", "SUBFAMILY",
                "SUPERTRIBE", "TRIBE", "SUBTRIBE", "GENUS", "SPECIES", "SUBSPECIES", "NAME"));

        System.out.println(bugData);


        for (int i = 0; i<classList.size(); i++) {
            if (bugData.has(classList.get(i))) {
                System.out.println(bugData.get(classList.get(i)).toString());
                if (!bugData.get(classList.get(i)).toString().trim().equals("")) {
                    classificationText += classList.get(i) + ": <i>" + (bugData.get(classList.get(i)).toString()) + "</i>";
                    if (classList.size() - 1 != i) {
                        classificationText += "<br>";
                    }
                }
            }
        }
        textToShow.setText(Html.fromHtml(classificationText));
        textToShow.setTextIsSelectable(true);
        textToShow.setLineSpacing(8,1);
        scrollLayout.addView(relativeLayout);
    }

    public void loadPublicDescription(String bugId, final LinearLayout scrollLayout, final FragmentManager fragmentManager) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout templateLayout = (LinearLayout) inflater.inflate(R.layout.template_bugdata, null);

        final Map<String,String> headersToSend = new HashMap<>();
        headersToSend.put("Session-cookie", User.sessionCookie);
        headersToSend.put("Insect-id",bugId);

        TaskQueue.prepare().backgroundTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                String response = Requester.request("/home/getPublicDescription",headersToSend,null);
                System.out.println("INFO INITIALIZER PUBDESC: "+response);

                ResponseCheck.isResponseValid(response,context,fragmentManager);
                try {
                    responseJSON = new JSONObject(response);
                    responseArray = responseJSON.getJSONArray("public_description");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).guiTask(new Runnable() {
            @Override
            public void run() {
                try {
                    //show what you got in response on gui
                    System.out.println("GOT RESPONSE !" + responseJSON);

                    TextView subTitle = templateLayout.findViewById(R.id.bugData_subtitle);
                    subTitle.setText(context.getResources().getString(R.string.subtitle_public_description));
                    TextView contents = templateLayout.findViewById(R.id.content_text);

                    if (responseArray.length() == 0) {
                        contents.setHint(Html.fromHtml("<i>No public description yet!\nClick edit to add it.</i>"));
                    } else {
                        JSONObject insectObject = responseArray.getJSONObject(0);
                        if (insectObject.has("PUB_DESC")) {
                            if (insectObject.get("PUB_DESC").toString().equals("")) {
                                contents.setHint(Html.fromHtml("<i>No public description yet!\nClick edit to add it.</i>"));

                            } else {
                                contents.setTextIsSelectable(true);
                                contents.setText(insectObject.get("PUB_DESC").toString());
                            }
                        }
                    }
                    //caching public description so i won't have to request it again.
                    Insect.setPublicDescription(responseJSON.toString());

                    scrollLayout.addView(templateLayout);
                    InterfaceFeatures.fadeIn(templateLayout, 500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).subscribeMe();

    }

    public void loadInsectInfo (final JSONObject bugData, final LinearLayout scrollLayout) {

        TaskQueue.prepare().guiTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout templateLayout = (LinearLayout) inflater.inflate(R.layout.template_bugdata, null);
                final LinearLayout hiddenLayout = templateLayout.findViewById(R.id.layout_contentText);
                hiddenLayout.setVisibility(View.GONE);
                ImageView buttonForExpand = templateLayout.findViewById(R.id.bugData_optional_image);
                buttonForExpand.setVisibility(View.VISIBLE);

                TextView subTitle = templateLayout.findViewById(R.id.bugData_subtitle);
                subTitle.setText("INFORMATION");

                TextView contents = templateLayout.findViewById(R.id.content_text);
                String contentsForTextView = "";

                try {
                    contentsForTextView += "insect reference: "+bugData.get("URL").toString();
                    if (bugData.has("IMAGES")) {
                        JSONArray imagesArray = new JSONArray(bugData.get("IMAGES").toString());
                        for (int i = 0; i < imagesArray.length(); i++) {
                            JSONObject imageObject = (JSONObject)imagesArray.get(i);
                            contentsForTextView += "\n\n"+(i+1)+". image url: "+(imageObject.get("image_url").toString())+"\nauthor: "+(imageObject.get("author_url").toString());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                contents.setText(contentsForTextView);

                scrollLayout.addView(templateLayout);

                //set TextView so you can linkify in activity.
                infoWithLinks = contents;

                buttonForExpand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InterfaceFeatures.expandOrCollapseView(hiddenLayout);
                    }
                });
            }
        }).subscribeMe();

    }

    public String getExternalLinksText (JSONObject bugData) {

        String contentsForTextView = "";

        try {
            System.out.println(bugData);
            if (bugData.has("URL")) {
                contentsForTextView += context.getResources().getString(R.string.insect_reference) + bugData.get("URL").toString();
            }
            if (bugData.has("IMAGES")) {
                JSONArray imagesArray = new JSONArray(bugData.get("IMAGES").toString());
                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject imageObject = (JSONObject)imagesArray.get(i);
                    contentsForTextView += "\n\n"+(i+1)+context.getResources().getString(R.string.image_url)+(imageObject.get("image_url").toString())+"\n"+context.getResources().getString(R.string.author)+(imageObject.get("author_url").toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contentsForTextView;

    }

    public String getInsectInfo (final JSONObject bugData, final LinearLayout scrollLayout) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout templateLayout = (LinearLayout) inflater.inflate(R.layout.template_bugdata, null);
        final LinearLayout hiddenLayout = templateLayout.findViewById(R.id.layout_contentText);
        hiddenLayout.setVisibility(View.GONE);
        ImageView buttonForExpand = templateLayout.findViewById(R.id.bugData_optional_image);
        buttonForExpand.setVisibility(View.VISIBLE);

        TextView subTitle = templateLayout.findViewById(R.id.bugData_subtitle);
        subTitle.setText(context.getResources().getString(R.string.button_info));

        TextView contents = templateLayout.findViewById(R.id.content_text);
        String contentsForTextView = "";

        try {
            contentsForTextView += "insect reference: "+bugData.get("URL").toString();
            if (bugData.has("IMAGES")) {
                JSONArray imagesArray = new JSONArray(bugData.get("IMAGES").toString());
                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject imageObject = (JSONObject)imagesArray.get(i);
                    contentsForTextView += "\n\n"+(i+1)+". image url: "+(imageObject.get("image_url").toString())+"\nauthor: "+(imageObject.get("author_url").toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        contents.setText(contentsForTextView);

        //scrollLayout.addView(templateLayout);

        //set TextView so you can linkify in activity.
        infoWithLinks = contents;

        buttonForExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterfaceFeatures.expandOrCollapseView(hiddenLayout);
            }
        });

        return contents.getText().toString();

    }

    public void loadNotes (final String bugId, final Context context, final FragmentManager fragmentManager, final LinearLayout scrollLayout) {

        final Map<String,String> headersMap = new HashMap<>();

        TaskQueue.prepare().backgroundTask(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {

                headersMap.put("Session-cookie",User.sessionCookie);
                String response = Requester.request("/home/getProperties",headersMap,null);
                if (ResponseCheck.isResponseValid(response,context,fragmentManager) & User.isInsectInCollection(bugId,response) == true) {

                    //caching
                    User.userProperties = response;

                    //POSTAVLJANJE NOTOVA
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                    final LinearLayout templateLayout = (LinearLayout) inflater.inflate(R.layout.template_bugdata, null);

                    TextView subTitle = templateLayout.findViewById(R.id.bugData_subtitle);
                    subTitle.setText(context.getResources().getString(R.string.subtitle_notes));
                    System.out.println(Insect.getNotes());
                    TextView contents = templateLayout.findViewById(R.id.content_text);

                    if (Insect.getNotes().equals("") || Insect.getNotes()==null) {
                        contents.setText(Html.fromHtml("<i>No notes yet!\nClick edit to add them.</i>"));
                    } else {
                        contents.setText(Html.fromHtml("<i>"+Insect.getNotes()+"</i>"));
                    }

                    TaskQueue.prepare().guiTask(new Runnable() {
                        @Override
                        public void run() {
                            scrollLayout.addView(templateLayout);
                            InterfaceFeatures.fadeIn(templateLayout, 500);

                        }
                    }).subscribeMe();
                }
            }
        }).subscribeMe();

    }
}
