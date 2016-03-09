package com.example.osorekoxuan.cardiact;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
public class AEDActivity extends Activity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private TextView aedAddress, aedLocation, aedNumber, aedZip;
    private double latitude, longitude;
    final String DEBUGTAG = "AED Activity:";
    public BootstrapButton pathFindingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aed);

        aedAddress = (TextView) findViewById(R.id.aed_address);
        aedLocation = (TextView) findViewById(R.id.aed_location);
        aedNumber = (TextView) findViewById(R.id.aed_nubmer);
        aedZip = (TextView) findViewById(R.id.aed_zip);
        pathFindingBtn = (BootstrapButton) findViewById(R.id.path_finding);
        pathFindingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AEDActivity.this, DirectionActivity.class);
                Bundle b = new Bundle();
                Helper.isPathDrawn = false;
                b.putDouble("longitude", latitude); //Your id
                b.putDouble("latitude", longitude); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });
        Bundle b = getIntent().getExtras();
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");
        Log.d("AED Lat", Double.toString(latitude));
        Log.d("AED Long", Double.toString(longitude));
        ParseGeoPoint geoPoint = new ParseGeoPoint(latitude, longitude);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("AED_DATA");
        query.whereEqualTo("LOCATION", geoPoint);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> pointList, ParseException e) {
                if (e == null) {
                    for(ParseObject point : pointList){
                        aedAddress.setText(point.getString("ADD_FULL"));
                        aedLocation.setText(point.getString("DEV_LOC"));
                        aedNumber.setText(point.getString("DEV_COUNT"));
                        aedZip.setText(point.getString("POSTAL_CD"));
                        Log.d("AED Detailed", point.getString("ADD_FULL"));
                    }
                    Log.d("AED Detailed", " get detailed information successfully");
                } else {
                    Log.d("AED Detailed"," get detailed information error");
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
         super.onBackPressed();
    }
}
