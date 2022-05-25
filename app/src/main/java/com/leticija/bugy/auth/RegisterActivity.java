package com.leticija.bugy.auth;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.leticija.bugy.InterfaceFeatures;
import com.leticija.bugy.R;
import com.leticija.bugy.auth.Authenticator;
import com.leticija.bugy.auth.User;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        final Context context = getApplicationContext();

        //user i welcome se ne smiju videti, samo toolbar
        Group welcomeGroup = findViewById(R.id.groupWelcome);
        welcomeGroup.setVisibility(View.INVISIBLE);


        final Button registerButton = findViewById(R.id.button3);
        final EditText usernameText = findViewById(R.id.username_input);
        final EditText passwordText = findViewById(R.id.password_input);
        final TextView textView = findViewById(R.id.textView8);
        final ImageView spinner = findViewById(R.id.imageView_loading2);
        final EditText emailEditText = findViewById(R.id.email_input);
        final EditText passwordAgainEditText = findViewById(R.id.password_input_again);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    InterfaceFeatures.setButtonEffect(R.color.button_green,R.color.success,registerButton,context);
                }

                String username = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                String email = emailEditText.getText().toString();

                if (!password.equals(passwordAgainEditText.getText().toString())) {
                    InterfaceFeatures.changeTextViewVisibility(textView,true,context.getResources().getString(R.string.message_pass_dont_match),R.color.warning);
                }
                else if (username.equals("") || password.equals("") || email.equals("")) {
                    InterfaceFeatures.changeTextViewVisibility(textView,true,context.getResources().getString(R.string.message_fill_everything),R.color.warning);
                } else {

                    InterfaceFeatures.setRotateAnimation(spinner);
                    spinner.setVisibility(View.VISIBLE);

                    User.setUsername(username);
                    User.setPassword(password);
                    Authenticator authenticator = new Authenticator(textView, spinner);
                    authenticator.register("/register", null, email,context);

                    System.out.println("REGISTERING: " + User.getUsername());
                }
            }
        });
    }
}
