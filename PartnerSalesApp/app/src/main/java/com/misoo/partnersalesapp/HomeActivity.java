package com.misoo.partnersalesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        CardView contacts = findViewById(R.id.card_1);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        CardView courses = findViewById(R.id.card_2);
        courses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this,  R.xml.pref_notification, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserPref();
    }

    private void updateUserPref() {

        TextView textPartnerPrefUserName = (TextView)findViewById(R.id.partnerDisplayName);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = pref.getString("user_display_name", "");
//        String webUrl = pref.getString("user_favorite_social", "");

        textPartnerPrefUserName.setText(userName);
    }
}