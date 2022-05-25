package com.leticija.bugy;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leticija.bugy.activities.Insect;
import com.leticija.bugy.auth.User;
import com.leticija.bugy.concurrent.TaskQueue;
import com.leticija.bugy.net.Requester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditInsectInitializer {

    ScrollView scrollView;
    Context context;
    JSONObject responseJSON;
    JSONArray responseArray;
    public static List<AutoCompleteTextView> aCTextViews;
    public static List<TextView> classGroupTextViews;
    public static EditText notesEditText;
    public static EditText publicDescEditText;

    public EditInsectInitializer(ScrollView scrollView, Context context) {
        this.scrollView = scrollView;
        this.context = context;
    }

    public void loadClassificationFields (final LinearLayout scrollLayout, String bugData) throws JSONException {

        classGroupTextViews = new ArrayList<>();

        ArrayList<String> classList = new ArrayList<>(Arrays.asList("PHYLUM", "SUBPHYLUM",
                "CLASS", "ORDER", "SUBORDER", "INFRAORDER", "SUPERFAMILY", "FAMILY", "SUBFAMILY",
                "SUPERTRIBE", "TRIBE", "SUBTRIBE", "GENUS", "SPECIES", "SUBSPECIES", "NAME"));

        aCTextViews = new ArrayList<>();

        for (int i=0;i<classList.size();i++) {

            final String classGroupText = classList.get(i);//klasifikacijska grupa
            String name = "";//text

            //ako već postoji u bugdati (jer bum ovu funkciju koristila i kod custom adda)
            if (bugData != null) {
                JSONObject dataObject = new JSONObject(bugData);

                if (dataObject.has(classGroupText)) { //još provjeriti jel ima tu klasifikacijsku grupu !
                    name = dataObject.get(classGroupText).toString();
                }
            }

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            final LinearLayout autoCompleteTemplate = (LinearLayout) inflater.inflate(R.layout.template_auto_complete, null);

            LinearLayout templateLayout = autoCompleteTemplate.findViewById(R.id.layout_autoComplete);

            //FIND views you'll need
            TextView groupTextView = autoCompleteTemplate.findViewById(R.id.classGroup_textView);
            groupTextView.setText(classGroupText); //set name of classGroup in view
            Button button = autoCompleteTemplate.findViewById(R.id.submit_button_edit);

            final AutoCompleteTextView autoCompleteTextView = templateLayout.findViewById(R.id.auto_complete_text_view);

            autoCompleteTextView.setText(name); //set name in autoCompleteTextView if exists.

            //add new element to arraylist
            aCTextViews.add(autoCompleteTextView);
            classGroupTextViews.add(groupTextView);

            if (i!=classList.size()-1) {
                final int finalI = i;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //UZIMANJE ZADNJEG U KOJEM JE NEKAJ PISALO DA SE NE PONUĐUJU SUBGRUPE KOJE NISU DIO NEKE GRUPE (ta grupa možda nema subgrupe)
                        AutoCompleteTextView zadnjiUKojemNekajPise = aCTextViews.get(finalI);
                        int smanjujuciIndeks = finalI;
                        while (zadnjiUKojemNekajPise.getText().toString().equals("") && smanjujuciIndeks!=0) {
                            smanjujuciIndeks--;
                            zadnjiUKojemNekajPise = aCTextViews.get(smanjujuciIndeks);
                        }
                        System.out.println("ZADNJI U KOJEM NEKAJ PIŠE: "+zadnjiUKojemNekajPise.getText().toString());
                        System.out.println("NJEGOVA GRUPA: "+classGroupTextViews.get(smanjujuciIndeks).getText().toString());

                        final AutoCompleteTextView sljedeći = aCTextViews.get(finalI + 1);
                        final String groupName = zadnjiUKojemNekajPise.getText().toString();
                        final String groupToGet = classGroupTextViews.get(finalI+1).getText().toString();

                        //requestanje...
                        final int finalSmanjujućiIndeks = smanjujuciIndeks;
                        TaskQueue.prepare().backgroundTask(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void run() {

                                Map<String,String> headersMap = new HashMap<>();
                                headersMap.put("Group",classGroupTextViews.get(finalSmanjujućiIndeks).getText().toString());
                                headersMap.put("Group-name",groupName);
                                headersMap.put("GroupToGet",groupToGet);
                                String response = Requester.request("/home/getSubgroups",headersMap,null);
                                System.out.println("RESPONSE: "+response);

                                try {
                                    JSONObject responseJSON = new JSONObject(response);
                                    JSONArray jsonArray = responseJSON.getJSONArray("ClassGroupElems");

                                    //izbrojiti kolko ima ovih koji nisu null da mogu postaviti velicinu arraya
                                    int duljina = 0;
                                    for (int i = 0;i<jsonArray.length();i++) {
                                        JSONObject object = (JSONObject) jsonArray.get(i);
                                        if (object.has(groupToGet)) {
                                            duljina++;
                                        }
                                    }

                                    int numberOfFound = jsonArray.length();
                                    final String[] arrayForAdapter = new String[duljina];

                                    int j = 0;
                                    for (int i = 0;i<numberOfFound;i++) {

                                        JSONObject objekt = (JSONObject) jsonArray.get(i);
                                        if (objekt.has(groupToGet)) {
                                            String gottenGroupName = objekt.get(groupToGet).toString();
                                            System.out.println(gottenGroupName);
                                            arrayForAdapter[j] = (String) objekt.get(groupToGet);
                                            j++;
                                        }
                                    }

                                    TaskQueue.prepare().guiTask(new Runnable() {
                                        @Override
                                        public void run() {

                                            ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.select_dialog_item, arrayForAdapter);
                                            sljedeći.setThreshold(1);
                                            sljedeći.setAdapter(adapter);


                                        }
                                    }).subscribeMe();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }).subscribeMe();

                        //FOKUSIRANJE NA SLJEDEĆEG
                        sljedeći.requestFocus();
                        sljedeći.setSelection(sljedeći.getText().length());
                    }
                });
            }

            LinearLayout layoutForClassification = scrollLayout.findViewById(R.id.layoutForClassification);
            layoutForClassification.addView(templateLayout);
        }

    }

    public void loadPublicDescription (final LinearLayout scrollLayout, final String pubDesc) {

        TaskQueue.prepare().guiTask(new Runnable() {
            @Override
            public void run() {

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout template = (LinearLayout) inflater.inflate(R.layout.template_edit, null);
                LinearLayout templateLayout = template.findViewById(R.id.linearLayout_editBug);

                TextView subtitle = templateLayout.findViewById(R.id.editBug_subtitle);
                subtitle.setText(context.getResources().getString(R.string.subtitle_public_description));

                publicDescEditText = templateLayout.findViewById(R.id.editBug_editText);

                try {
                    JSONObject insectObject = new JSONObject(pubDesc);
                    JSONArray jsonArray = insectObject.getJSONArray("public_description");
                    System.out.println("ARRAY IN EDITINSECTINITIALIZER: "+jsonArray);
                    if (jsonArray.length() == 0) {
                        System.out.println("LENGTH OF ARRAY IS 0");
                        publicDescEditText.setHint(Html.fromHtml("<i>No public description yet!\nClick edit to add it.</i>"));
                    } else {
                        System.out.println("LENGTH OF ARRAY IS NOT 0");
                        JSONObject objectOne = jsonArray.getJSONObject(0);
                        System.out.println("JSONARRAY: "+jsonArray);
                        System.out.println("JSONOBJECT: "+objectOne);
                        if (objectOne.has("PUB_DESC")) {
                            publicDescEditText.setText(objectOne.get("PUB_DESC").toString());
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (pubDesc==null) {
                    publicDescEditText.setHint(Html.fromHtml("<i>No public description yet!\nClick edit to add it.</i>"));
                }


                scrollLayout.addView(templateLayout);

            }
        }).subscribeMe();

    }

    public void loadNotes (final LinearLayout scrollLayout, final String notes, String bugId) {

        if (!User.isInsectInCollection(bugId,User.getUserProperties())) {
            System.out.println(User.getUserProperties());
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            final LinearLayout template = (LinearLayout) inflater.inflate(R.layout.template_edit, null);
            LinearLayout templateLayout = template.findViewById(R.id.linearLayout_editBug);
            notesEditText = templateLayout.findViewById(R.id.editBug_editText);
            return;
        }

        TaskQueue.prepare().guiTask(new Runnable() {
            @Override
            public void run() {

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout template = (LinearLayout) inflater.inflate(R.layout.template_edit, null);
                LinearLayout templateLayout = template.findViewById(R.id.linearLayout_editBug);

                TextView subtitle = templateLayout.findViewById(R.id.editBug_subtitle);
                subtitle.setText(context.getResources().getString(R.string.subtitle_notes));

                notesEditText = templateLayout.findViewById(R.id.editBug_editText);
                notesEditText.setText(notes);

                if (notes==null || notes.equals("")) {
                    notesEditText.setHint(Html.fromHtml("<i>No personal notes yet!</i>"));
                }

                scrollLayout.addView(templateLayout);

            }
        }).subscribeMe();
    }

    public static boolean isAlreadyInCollection(JSONObject bugObject, String userProperties, Context context, FragmentManager fragmentManager) throws JSONException {

        boolean owns = true;

        String bugID = (String) bugObject.get("KEY");

        JSONObject propertiesObject = new JSONObject(userProperties);
        JSONArray array = propertiesObject.getJSONArray("properties");
        for (int i=0;i<array.length();i++) {
            JSONObject object = (JSONObject) array.get(i);
            if (object.get("INSECT_ID").equals(bugID)) {
                break;
            }
            owns = false;
        }
        if (array.length()==0) {
            owns = false;
        }

        return owns;
    }
}
