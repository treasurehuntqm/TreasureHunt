package com.th.monicadzhaleva.treasurehunt;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SplashScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        Button logInButton = (Button) findViewById(R.id.loginbutton);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));

            }
        });

        Button registerButton = (Button) findViewById(R.id.registerbutton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashScreen.this, RegisterActivity.class));

            }
        });
    }

}
