package com.example.dov.chatappopenu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

// TODO: delete this class and view
public class MainRouter extends ActionBarActivity {
    final static int PERMISSION_READ_STATE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_router);

        // set the dictionary to the first one in the array
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = prefs.getString("uid", "-1");
        if (uid.equals("-1")) {
            Intent signIn = new Intent(this, SignIn.class);
            startActivity(signIn);
        } else {
            Intent mainPage = new Intent(this, MainPage.class);
            startActivity(mainPage);
        }
    }
}