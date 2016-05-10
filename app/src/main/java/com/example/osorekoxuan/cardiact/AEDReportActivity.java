package com.example.osorekoxuan.cardiact;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class AEDReportActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    // UI references.
    private TextView usernameView, emailView;
    private NavigationView navigationView;
    private EditText aedAddress, aedFacility, aedFacilityType, deviceLocation, deviceNumber, postCode, unitType, unitNumber;
    private View mProgressView;
    private View mRegisterFormView;
    String objectId;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aed_report);
        // Set up the register form.

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        aedAddress = (EditText) findViewById(R.id.aed_address);
        aedFacility = (EditText) findViewById(R.id.aed_facility);
        aedFacilityType = (EditText) findViewById(R.id.aed_facility_type);
        deviceLocation = (EditText) findViewById(R.id.aed_device_location);
        deviceNumber = (EditText) findViewById(R.id.aed_device_number);
        postCode = (EditText) findViewById(R.id.aed_post_code);
        unitType = (EditText) findViewById(R.id.aed_unit_number);
        unitNumber = (EditText) findViewById(R.id.aed_unit_type);

        Bundle b = getIntent().getExtras();

        if(b != null) {
            objectId = b.getString("objectId");
        }
        if(objectId != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("AED_DATA");
            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                public void done(ParseObject aed, ParseException e) {
                    if (e == null) {
                        latitude = aed.getParseGeoPoint("LOCATION").getLatitude();
                        longitude = aed.getParseGeoPoint("LOCATION").getLongitude();

                        aedAddress.setText(aed.getString("ADD_FULL"));
                        deviceLocation.setText(aed.getString("DEV_LOC"));
                        deviceNumber.setText(aed.getString("DEV_COUNT"));
                        postCode.setText(aed.getString("POSTAL_CD"));
                        aedFacility.setText(aed.getString("FACI_NAM"));
                        aedFacilityType.setText(aed.getString("FACI_TYPE"));
                        unitType.setText(aed.getString("UNIT_TYPE"));
                        unitNumber.setText(aed.getString("UNIT"));
                    } else {
                        Log.e("AED Detailed", " get detailed information error");
                    }
                }
            });
        }

        BootstrapButton profileSubmit = (BootstrapButton) findViewById(R.id.aed_submit);
        profileSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSubmit();
            }
        });

        mRegisterFormView = findViewById(R.id.event_form);
        mProgressView = findViewById(R.id.event_progress);

        checkLoginStatus();
    }

    private void attemptSubmit() {
        // Reset errors.
        aedAddress.setError(null);

        // Store values at the time of the login attempt.
        String location = aedAddress.getText().toString();
        String facility = aedFacility.getText().toString();
        String facilityType = aedFacilityType.getText().toString();
        String dNumber = deviceNumber.getText().toString();
        String dLocation = deviceLocation.getText().toString();
        String postcode = postCode.getText().toString();
        String unit = unitNumber.getText().toString();
        String unittype = unitType.getText().toString();

        ParseGeoPoint locationGEO = getLocationFromAddress(location);


        boolean cancel = false;
        View focusView = null;

        // Check for a valid username, if the user entered one.
        if (TextUtils.isEmpty(location)) {
            aedAddress.setError(getString(R.string.error_field_required));
            focusView = aedAddress;
            cancel = true;
        }

        if(objectId != null) {
            ParseObject aed = new ParseObject("AED_UPDATE");
            aed.put("ADD_FULL", location);
            aed.put("DEV_LOC", dLocation);
            aed.put("DEV_COUNT", dNumber);
            aed.put("POSTAL_CD", postcode);
            aed.put("FACI_NAM", facility);
            aed.put("FACI_TYPE", facilityType);
            aed.put("UNIT_TYPE", unittype);
            aed.put("UNIT", unit);
            if(locationGEO == null) {
                aed.put("LOCATION", new ParseGeoPoint(latitude,longitude));
            }else {
                aed.put("LOCATION", locationGEO);
            }
            aed.put("ID", objectId);
            aed.saveInBackground();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("AED_DATA");
            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                public void done(ParseObject aed, ParseException e) {
                    if (e == null) {
                        aed.put("REPORTED", 1);
                        aed.saveInBackground();
                    } else {
                        Log.e("AED Detailed", " get detailed information error");
                    }
                }
            });

            ParseQuery pushQuery = ParseInstallation.getQuery();
            pushQuery.whereEqualTo("Roles", "Admin");
            ParsePush push = new ParsePush();
            push.setQuery(pushQuery); // Set our Installation query
            push.setMessage("The AED at: " + aed.getString("ADD_FULL") + " is reported!");
            push.sendInBackground();
        }
        // Retrieve the object by id
        Intent intent = new Intent(AEDReportActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public ParseGeoPoint getLocationFromAddress(String strAddress){

        strAddress = strAddress + ", Toronto, ON";
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        ParseGeoPoint point = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);

            Log.d("Address Lat", Double.toString(location.getLatitude()));
            Log.d("Address Long", Double.toString(location.getLongitude()));

            point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

            return point;
        }catch (Exception ex){

        }
        return point;
    }


    public void checkLoginStatus(){
        Menu menuNav = navigationView.getMenu();
        ParseUser currentUser = ParseUser.getCurrentUser();
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
            Intent intent = new Intent(AEDReportActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(AEDReportActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_register) {
            Intent intent = new Intent(AEDReportActivity.this, RegisterActivity.class);
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
            Intent intent = new Intent(AEDReportActivity.this, ProfileViewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ins){
            Intent intent = new Intent(AEDReportActivity.this, InstructionActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_contact){
            Intent intent = new Intent(AEDReportActivity.this, ContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

