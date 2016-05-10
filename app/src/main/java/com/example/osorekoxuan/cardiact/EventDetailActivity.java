package com.example.osorekoxuan.cardiact;


import android.*;
import android.app.Activity;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
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
public class EventDetailActivity extends ListActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private TextView usernameView, emailView;
    private NavigationView navigationView;
    private TextView eventLocation, eventTime, eventStatus, eventPerson;
    private LinearLayout personalForm;
    String objectId;
    ParseUser currentUser;
    BootstrapButton acceptButton, pathfindingButton;
    ArrayList<ListViewItem> item = new ArrayList<>();
    ListView eventList;
    double totalDistance = 10000;
    double lat, lng;
    ParseGeoPoint mLocation, eLocation, AEDLocation, selectLocation;
    Intent intent;
    Bundle bundle;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

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

        checkLoginStatus();

        currentUser = ParseUser.getCurrentUser();

        eventList = getListView();

        acceptButton = (BootstrapButton) findViewById(R.id.event_accept);
        pathfindingButton = (BootstrapButton) findViewById(R.id.event_path_finding);

        Bundle b = getIntent().getExtras();

        final double latitude = b.getDouble("latitude");
        final double longitude = b.getDouble("longitude");
        eLocation = new ParseGeoPoint(latitude, longitude);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Emergency");
        query.whereEqualTo("Location", eLocation);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> eventList, ParseException e) {
                if (e == null) {
                    for (ParseObject event : eventList) {
                        objectId = event.getObjectId();

                        ListViewItem listViewItem = new ListViewItem();

                        if (event.getString("Name") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "Location";
                            listViewItem.Context = event.getString("Name");
                            item.add(listViewItem);
                        }

                        if (event.getString("Time") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "Occurred Time";
                            listViewItem.Context = event.getString("Time");
                            item.add(listViewItem);
                        }

                        if (event.getString("Status") != null) {
                            listViewItem = new ListViewItem();
                            listViewItem.Title = "Status";
                            listViewItem.Context = event.getString("Status");
                            item.add(listViewItem);
                        }

                        if ("Finished".equals(event.getString("Status"))) {
                            acceptButton.setVisibility(View.INVISIBLE);
                        }
                        setListAdapter(new ListViewAdapter(EventDetailActivity.this, item));
                    }
                } else {
                    Log.e("Event Detailed", " get detailed information error");
                }
            }
        });
        if(currentUser != null) {
            ParseQuery<ParseObject> respQuery = ParseQuery.getQuery("Response");

            respQuery.whereEqualTo("eventGeo", eLocation);
            respQuery.whereEqualTo("userId", currentUser.getObjectId());

            respQuery.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> eventList, ParseException e) {
                    if (e == null) {
                        if (eventList.size() != 0) {
                            acceptButton.setVisibility(View.INVISIBLE);
                        } else {
                            pathfindingButton.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Log.e("Response Detailed", " get Response information error");
                    }
                }
            });
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentUser != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
                    builder.setMessage("Are you sure to accept this event?")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ParseObject event = new ParseObject("Response");
                                    event.put("userId", currentUser.getObjectId());
                                    event.put("eventGeo", eLocation);
                                    event.saveInBackground();

                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Emergency");
                                    query.getInBackground(objectId, new GetCallback<ParseObject>() {
                                        public void done(ParseObject event, ParseException e) {
                                            if (e == null) {
                                                event.put("Status", "Responded");
                                                event.saveInBackground();

                                                ParseQuery pushQuery = ParseInstallation.getQuery();
                                                pushQuery.whereEqualTo("Roles", "Admin");
                                                ParsePush push = new ParsePush();
                                                push.setQuery(pushQuery); // Set our Installation query
                                                push.setMessage("The Emergency at: " + event.getString("Name") + " has response!");
                                                push.sendInBackground();
                                            } else {
                                                Log.e("Event Detailed", " get event error");
                                            }
                                        }
                                    });

                                    acceptButton.setVisibility(View.INVISIBLE);
                                    pathfindingButton.setVisibility(View.VISIBLE);

                                    finish();
                                    startActivity(getIntent());
                                }
                            })
                            .setNegativeButton("Cancel", null);
                    builder.show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
                    builder.setMessage("Please login first!")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(EventDetailActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("Cancel", null);
                    builder.show();
                }
            }
        });

        pathfindingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(EventDetailActivity.this, DirectionActivity.class);
                bundle = new Bundle();
                Helper.isPathDrawn = false;
                bundle.putString("title", "aed and event");
                bundle.putDouble("eventLat", latitude); //Your id
                bundle.putDouble("eventLng", longitude); //Your id

                // Getting LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                // Creating a criteria object to retrieve provider
                Criteria criteria = new Criteria();

                // Getting the name of the best provider
                String provider = locationManager.getBestProvider(criteria, true);

                // Getting Current Location
                if (ContextCompat.checkSelfPermission(EventDetailActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(provider);
                }
                if (location == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailActivity.this);
                    builder.setMessage("Sorry, your location information could not be accessed!")
                            .setNegativeButton("Cancel", null);
                    builder.show();
                    return;
                } else {
                    mLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                }
                // 3
                ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery("AED_DATA");
                // 4
                mapQuery.whereWithinKilometers("LOCATION", mLocation, 0.5);
                // 6

                mapQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        // Handle the results
                        for (ParseObject post : objects) {
                            AEDLocation = post.getParseGeoPoint("LOCATION");

                            double distance = mLocation.distanceInKilometersTo(AEDLocation) + AEDLocation.distanceInKilometersTo(eLocation);

                            if (distance < totalDistance) {
                                selectLocation = AEDLocation;
                                totalDistance = distance;
                            }
                        }

                        if (selectLocation == null) {
                            ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery("AED_DATA");
                            mapQuery.whereNear("LOCATION", mLocation);
                            mapQuery.setLimit(1);
                            mapQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    // Handle the results
                                    for (ParseObject post : objects) {
                                        selectLocation = post.getParseGeoPoint("LOCATION");
                                    }

                                    bundle.putDouble("latitude", selectLocation.getLatitude()); //Your id
                                    bundle.putDouble("longitude", selectLocation.getLongitude()); //Your id

                                    intent.putExtras(bundle); //Put your id to your next Intent
                                    startActivity(intent);

                                }
                            });
                        } else {
                            bundle.putDouble("latitude", selectLocation.getLatitude()); //Your id
                            bundle.putDouble("longitude", selectLocation.getLongitude()); //Your id

                            intent.putExtras(bundle); //Put your id to your next Intent
                            startActivity(intent);
                        }
                    }
                });

            }
        });
    }


    public void checkLoginStatus(){
        Menu menuNav = navigationView.getMenu();

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
            Intent intent = new Intent(EventDetailActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(EventDetailActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_register) {
            Intent intent = new Intent(EventDetailActivity.this, RegisterActivity.class);
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
            Intent intent = new Intent(EventDetailActivity.this, ProfileViewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ins){
            Intent intent = new Intent(EventDetailActivity.this, InstructionActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_contact){
            Intent intent = new Intent(EventDetailActivity.this, ContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
