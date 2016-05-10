package com.example.osorekoxuan.cardiact;

import android.*;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    TextView eventAddress, eventDistance, eventTime;
    ImageButton acceptButton, declineButton;
    ParseUser currentUser;
    double totalDistance = 10000;
    ParseGeoPoint mLocation, eLocation, AEDLocation, selectLocation;
    Intent intent;
    Bundle bundle;
    String objectId;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        currentUser = ParseUser.getCurrentUser();

        eventAddress = (TextView) findViewById(R.id.event_address);
        eventDistance = (TextView) findViewById(R.id.event_distance);
        eventTime = (TextView) findViewById(R.id.event_time);

        acceptButton = (ImageButton) findViewById(R.id.accept_button);
        declineButton = (ImageButton) findViewById(R.id.decline_button);

        Bundle b = getIntent().getExtras();
        objectId = b.getString("objectId");

        ParseQuery< ParseObject > query = ParseQuery.getQuery("Emergency");

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(provider);
        }
        if(location != null) {
            mLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Sorry, your location information could not be accessed!")
                    .setNegativeButton("Cancel", null);
            builder.show();
            return;
        }

        // Retrieve the object by id
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject event, ParseException e) {
                if (e == null) {
                    String address = event.getString("Name");
                    eLocation = event.getParseGeoPoint("Location");

                    String date = event.getString("Time");

                    double distance = mLocation.distanceInKilometersTo(eLocation);

                    eventAddress.setText(address);
                    eventDistance.setText("Distance: " + String.format("%.2f", distance) + " Km");
                    eventTime.setText("Occurred Time: " + date);

                }
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
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

                                intent = new Intent(FullscreenActivity.this, DirectionActivity.class);
                                bundle = new Bundle();
                                Helper.isPathDrawn = false;
                                bundle.putString("title", "aed and event");
                                bundle.putDouble("eventLat", eLocation.getLatitude()); //Your id
                                bundle.putDouble("eventLng", eLocation.getLongitude()); //Your id
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
                        })
                        .setNegativeButton("Cancel", null);
                builder.show();
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                builder.setMessage("Are you sure to decline this event?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(FullscreenActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", null);
                builder.show();
            }
        });

    }

}
