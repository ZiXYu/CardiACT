package com.example.osorekoxuan.cardiact;


import android.app.Activity;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class AEDActivity extends ListActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private TextView usernameView, emailView;
    private NavigationView navigationView;
    private TextView aedAddress, aedLocation, aedNumber, aedZip, facilityName, facilityType, unitNumLabel, unitNum;
    private double latitude, longitude;
    final String DEBUGTAG = "AED Activity:";
    public BootstrapButton pathFindingBtn, reportErrotBun;
    private AwesomeTextView warningText;
    private String objectId;
    ArrayList<ListViewItem> item = new ArrayList<>();
    ListView AEDList;
    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aed);

        currentUser = ParseUser.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        usernameView = (TextView) headerView.findViewById(R.id.username);
        emailView = (TextView) headerView.findViewById(R.id.email);

        Bundle b = getIntent().getExtras();
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");
        Log.d("AED Lat", Double.toString(latitude));
        Log.d("AED Long", Double.toString(longitude));
        ParseGeoPoint geoPoint = new ParseGeoPoint(latitude, longitude);

        warningText = (AwesomeTextView) findViewById(R.id.warning_text);
        warningText.setVisibility(View.GONE);

        AEDList = getListView();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("AED_DATA");
        query.whereEqualTo("LOCATION", geoPoint);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> pointList, ParseException e) {
                if (e == null) {
                    for (ParseObject point : pointList) {
                        objectId = point.getObjectId();
                        ListViewItem listViewItem = new ListViewItem();

                        if (point.getString("ADD_FULL") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "AED Address";
                            listViewItem.Context = point.getString("ADD_FULL");
                            item.add(listViewItem);
                        }

                        if (point.getString("FACI_NAM") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "Facility Name";
                            listViewItem.Context = point.getString("FACI_NAM");
                            item.add(listViewItem);
                        }

                        if (point.getString("FACI_TYPE") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "Facility Type";
                            listViewItem.Context = point.getString("FACI_TYPE");
                            item.add(listViewItem);
                        }
                        if (point.getString("DEV_LOC") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "Device Location";
                            listViewItem.Context = point.getString("DEV_LOC");
                            item.add(listViewItem);
                        }
                        if (point.getString("DEV_COUNT") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "Device Number";
                            listViewItem.Context = point.getString("DEV_COUNT");
                            item.add(listViewItem);
                        }
                        if (point.getString("POSTAL_CD") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "ZIP code";
                            listViewItem.Context = point.getString("POSTAL_CD");
                            item.add(listViewItem);
                        }

                        if (point.getString("UNIT") != null && !"".equals(point.getString("UNIT"))) {
                            if (point.getString("UNIT_TYPE") != null && !"".equals(point.getString("UNIT_TYPE"))) {
                                listViewItem = new ListViewItem();
                                listViewItem.Title = "Unit Info";
                                listViewItem.Context = point.getString("UNIT_TYPE") + ". " + point.getString("UNIT");
                                item.add(listViewItem);
                            } else {
                                listViewItem = new ListViewItem();
                                listViewItem.Title = "Unit Info";
                                listViewItem.Context = point.getString("UNIT");
                                item.add(listViewItem);
                            }
                        }

                        if(point.getInt("REPORTED") == 1){
                            warningText.setVisibility(View.VISIBLE);
                        }
                        setListAdapter(new ListViewAdapter(AEDActivity.this, item));
                        Log.d("AED Detailed", point.getString("ADD_FULL"));
                    }
                    Log.d("AED Detailed", " get detailed information successfully");
                } else {
                    Log.d("AED Detailed", " get detailed information error");
                }
            }
        });

        pathFindingBtn = (BootstrapButton) findViewById(R.id.path_finding);
        pathFindingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AEDActivity.this, DirectionActivity.class);
                Bundle b = new Bundle();
                Helper.isPathDrawn = false;
                b.putString("title", "aed");
                b.putDouble("latitude", latitude); //Your id
                b.putDouble("longitude", longitude); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });

        reportErrotBun = (BootstrapButton) findViewById(R.id.report_error);
        reportErrotBun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUser == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AEDActivity.this);
                    builder.setMessage("Please login first!")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(AEDActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("Cancel", null);
                    builder.show();
                }else {
                    Intent intent = new Intent(AEDActivity.this, AEDReportActivity.class);
                    Bundle b = new Bundle();
                    b.putString("objectId", objectId);
                    intent.putExtras(b); //Put your id to your next Intent
                    startActivity(intent);
                }
            }
        });

        checkLoginStatus();
    }


    public void checkLoginStatus(){
        Menu menuNav = navigationView.getMenu();
        //ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            String name = currentUser.getString("firstname") + currentUser.getString("lastname");
            String email = currentUser.getEmail();

            if(name != null) {
                usernameView.setText(name);
            }
            if(email != null) {
                emailView.setText(email);
            }

            MenuItem loginItem = menuNav.findItem(R.id.nav_login);
            loginItem.setVisible(false);
            MenuItem registerItem = menuNav.findItem(R.id.nav_register);
            registerItem.setVisible(false);

        } else {
            // show the signup or login screen
            MenuItem profileItem = menuNav.findItem(R.id.nav_profile);
            profileItem.setVisible(false);

            MenuItem logoutItem = menuNav.findItem(R.id.nav_logout);
            logoutItem.setVisible(false);

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_home){
            Intent intent = new Intent(AEDActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(AEDActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_register) {
            Intent intent = new Intent(AEDActivity.this, RegisterActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure to logout?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ParseUser.logOut();
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .setNegativeButton("Cancel", null);
            builder.show();
        } else if (id == R.id.nav_profile){
            Intent intent = new Intent(AEDActivity.this, ProfileViewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ins){
            Intent intent = new Intent(AEDActivity.this, InstructionActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_contact){
            Intent intent = new Intent(AEDActivity.this, ContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
