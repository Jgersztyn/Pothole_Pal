package com.jgersztyn.pothole_pal;

import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.AdapterView;
import android.widget.Button;
import android.content.Intent;
import android.widget.ListView;

public class FirstAcivity extends AppCompatActivity {

    ActionBarDrawerToggle drawerToggle;
    Toolbar toolbar;
    DrawerLayout drawerLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_acivity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLay = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);

        //set up our button for interaction
        Button toMapButton = (Button)findViewById(R.id.ButtonToMap);
        //describe the action event to take place with this button
        toMapButton.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v) {
                        //v.getContext specifically refers to the context of the view that we are coding this in
                        //essentially, it refers to where we are
                        //the second argument describes where we will be going when the action is taken
                        Intent myIntent = new Intent(v.getContext(), MapsActivity.class);
                        startActivity(myIntent);
                    }
                });

        //button to get to settings
        Button toSettingsBut = (Button) findViewById(R.id.settingsButton);
        toSettingsBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstAcivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        //button to get to login page
        Button toLoginBut = (Button) findViewById(R.id.loginButton);
        toLoginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstAcivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

//        //I am unsure if there is any way we can get this to work
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLay, toolbar, R.string.drawer_open,
                R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        // Inflate the menu; this adds items to the action bar if it is present.



        getMenuInflater().inflate(R.menu.menu_first_acivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.map_id) {
            Intent openAboutActivityIntent = new Intent(this, MapsActivity.class);
            startActivity(openAboutActivityIntent);
            return true;
        }
        if(id == R.id.settings_id) {
            Intent openAboutActivityIntent = new Intent(this, SettingsActivity.class);
            startActivity(openAboutActivityIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}
