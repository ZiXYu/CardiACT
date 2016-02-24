package com.example.osorekoxuan.cardiact;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.opencsv.CSVReader;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*List<String[]> aedData = readCsv(this);

        for (int i=750; i<aedData.size(); i++){
            String[] AED= aedData.get(i);
            ParseObject aed_data = new ParseObject("AED_DATA");
            aed_data.put("ADD_FULL", AED[0]);
            aed_data.put("POSTAL_CD", AED[1]);
            aed_data.put("X", AED[2]);
            aed_data.put("Y", AED[3]);
            ParseGeoPoint point = new ParseGeoPoint(Double.parseDouble(AED[4]), Double.parseDouble(AED[5]));
            aed_data.put("LOCATION", point);
            aed_data.put("OBJECTID", AED[6]);
            aed_data.put("FACI_NAM", AED[7]);
            aed_data.put("FACI_TYPE", AED[8]);
            aed_data.put("UNIT", AED[9]);
            aed_data.put("UNIT_TYPE", AED[10]);
            aed_data.put("DEV_LOC", AED[11]);
            aed_data.put("DEV_COUNT", AED[12]);
            aed_data.saveInBackground();
        }*/
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public final List<String[]> readCsv(Context context) {
        List<String[]> questionList = new ArrayList<String[]>();
        AssetManager assetManager = context.getAssets();

        try {
            InputStream csvStream = assetManager.open("AED_DATA.csv");
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            String[] line;

            // throw away the header
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                questionList.add(line);
                Log.d("AED", line[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questionList;
    }
}
