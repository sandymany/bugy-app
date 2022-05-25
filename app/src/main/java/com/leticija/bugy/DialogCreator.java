package com.leticija.bugy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class DialogCreator extends AppCompatDialogFragment {

    final String title;
    final String message;
    final String positiveButton;
    final String negativeButton;
    Runnable r1 = null;
    Runnable r2 = null;

    @SuppressLint("ValidFragment")
    DialogCreator(String title, String message,String positiveButton,String negativeButton, Runnable r1,Runnable r2) {
        this.title = title;
        this.message = message;
        this.positiveButton = positiveButton;
        this.negativeButton = negativeButton;
        this.r1 = r1;
        this.r2 = r2;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.template_dialogue,null);

        TextView titleText = view.findViewById(R.id.dialogue_title);
        titleText.setText(title);
        TextView messageText = view.findViewById(R.id.dialogue_message);
        messageText.setText(message);

        builder.setView(view)
                .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("NEGATIVE BUTTON CLICKED!");
                        r1.run();
                    }
                })
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("POSITIVE BUTTON CLICKED!");
                        r2.run();
                    }
                });
        return builder.create();

    }
}
