package com.example.osorekoxuan.cardiact;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JINGNAN on 2016-02-29.
 */
public class DirectionActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    LatLng myLatLng;
    LatLng shopLatLng;
    private double des_latitude, des_longitude, mylat, mylng, eventLat, eventLng;
    Boolean isDirectionDrawn = false;
    private GoogleMap mMap;
    public LocationManager locationManager;
    private String gpsProvider, netProvider;
    String title;
    private String DEBUGTAG = "Direction Activity: ";
    BootstrapButton eventFinish, getAED, getVictim;
    ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
    Location location;

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d("LocationListener: ", "Location Changed");
                mylat = location.getLatitude();
                mylng = location.getLongitude();
                myLatLng = new LatLng(mylat, mylng);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        des_latitude = 0; des_longitude = 0; eventLng = 0; eventLat = 0;

        eventFinish = (BootstrapButton) findViewById(R.id.event_finish);
        getAED = (BootstrapButton) findViewById(R.id.get_aed);
        getVictim = (BootstrapButton) findViewById(R.id.get_event);

        Bundle b = getIntent().getExtras();
        title = b.getString("title");
        if("aed".equals(title)) {
            getVictim.setVisibility(View.GONE);
            des_latitude = b.getDouble("latitude");
            des_longitude = b.getDouble("longitude");
        }else if ("event".equals(title)){
            getAED.setVisibility(View.GONE);
            des_latitude = b.getDouble("eventLat");
            des_longitude = b.getDouble("eventLng");
        }else{
            getVictim.setVisibility(View.GONE);
            des_latitude = b.getDouble("latitude");
            des_longitude = b.getDouble("longitude");
            eventLat = b.getDouble("eventLat");
            eventLng = b.getDouble("eventLng");
        }
        parseGeoPoint.setLatitude(des_latitude);
        parseGeoPoint.setLongitude(des_longitude);
        Log.e("Title", title);
        Log.e("Des Lat", Double.toString(des_latitude));
        Log.e("Des Lng", Double.toString(des_longitude));

        eventFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DirectionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getAED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eventLat != 0 && eventLng != 0) {
                    Intent intent = new Intent(DirectionActivity.this, DirectionActivity.class);
                    Bundle bundle = new Bundle();
                    Helper.isPathDrawn = false;
                    bundle.putString("title", "event");
                    bundle.putDouble("eventLat", eventLat); //Your id
                    bundle.putDouble("eventLng", eventLng); //Your id
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(DirectionActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });

        getVictim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DirectionActivity.this, InstructionActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getPosition();

    }

    public void getPosition() {

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(provider);
        }

        if(location != null) {
            mylat = location.getLatitude();
            mylng = location.getLongitude();
            myLatLng = new LatLng(mylat, mylng);
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Sorry, your location information could not be accessed!")
                    .setNegativeButton("Cancel", null);
            builder.show();
            return;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        shopLatLng = new LatLng(des_latitude, des_longitude);

        mMap.addMarker(new MarkerOptions()
                        .position(shopLatLng)
                        .title("Destination")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(shopLatLng));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }

        moveToCurrentLocation(shopLatLng);

//        LocationManager locationManager = (LocationManager)
//                getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//
//        Location location = locationManager.getLastKnownLocation(locationManager
//                .getBestProvider(criteria, false));
//
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                2000, 1, this);
//
//
//
//        myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//        Log.d(DEBUGTAG, myLatLng.latitude +"   "+ myLatLng.longitude);

        new LongOperation().execute("");
    }


    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }



    private void zoomToPoints() {
        try {


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            //        for (Marker marker : markers) {
            builder.include(myLatLng);
            builder.include(shopLatLng);
            //        }
            LatLngBounds bounds = builder.build();

            int padding = 50; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            mMap.animateCamera(cu);
        }
        catch (Exception e)
        {
            ///possible error:
            /// java.lang.NullPointerException: Attempt to invoke interface method 'org.w3c.dom.NodeList org.w3c.dom.Document.getElementsByTagName(java.lang.String)' on a null object reference
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        myLatLng = new LatLng(latitude, longitude);

        zoomToPoints();

        if(!isDirectionDrawn) {
            new LongOperation().execute("");
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(!isDirectionDrawn) {
            //            zoomToPoints();
            //new LongOperation().execute("");
        }
    }

    private class LongOperation extends AsyncTask<String, Void, PolylineOptions> {

        private PolylineOptions getDirection() {
            try {
                GMapV2Direction md = new GMapV2Direction();
                Log.d("DA My Lat", Double.toString(myLatLng.latitude));
                Log.d("DA My Long", Double.toString(myLatLng.longitude));
                Log.d("DA Destination Lat", Double.toString(shopLatLng.latitude));
                Log.d("DA Destination Long", Double.toString(shopLatLng.longitude));
                Document doc = md.getDocument(myLatLng, shopLatLng,
                        GMapV2Direction.MODE_WALKING);

                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                PolylineOptions rectLine = new PolylineOptions().width(9).color(
                        Color.GREEN);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                isDirectionDrawn = true;

                return rectLine;
            }
            catch (Exception e)
            {
                ///possible error:
                ///java.lang.IllegalStateException: Error using newLatLngBounds(LatLngBounds, int): Map size can't be 0. Most likely, layout has not yet occured for the map view.  Either wait until layout has occurred or use newLatLngBounds(LatLngBounds, int, int, int) which allows you to specify the map's dimensions.
                return null;
            }

        }

        @Override
        protected PolylineOptions doInBackground(String... params) {
            PolylineOptions polylineOptions = null;
            try {
                polylineOptions = getDirection();
            } catch (Exception e) {
                Thread.interrupted();
            }
            return polylineOptions;
        }

        @Override
        protected void onPostExecute(PolylineOptions result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you

            mMap.clear();///TODO: clean the path only.

            if("aed".equals(title) || "aed and event".equals(title)) {
                ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery("AED_DATA");
                // 4
                mapQuery.whereEqualTo("LOCATION", parseGeoPoint);
                // 6
                mapQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        // Handle the results
                        for (ParseObject post : objects) {
                            double lat = post.getParseGeoPoint("LOCATION").getLatitude();
                            double lng = post.getParseGeoPoint("LOCATION").getLongitude();

                            Drawable circle;
                            if(post.getInt("REPORTED") == 1){
                                circle = getResources().getDrawable(R.drawable.ic_marker_yellow);
                            }else {
                                circle = getResources().getDrawable(R.drawable.ic_marker_green);
                            }
                            Canvas canvas = new Canvas();
                            Bitmap bitmap = Bitmap.createBitmap(circle.getIntrinsicWidth(), circle.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                            canvas.setBitmap(bitmap);
                            circle.setBounds(0, 0, circle.getIntrinsicWidth(), circle.getIntrinsicHeight());
                            circle.draw(canvas);
                            BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(bitmap);

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title("AED")
                                    .snippet(post.getString("ADD_FULL"))
                                    .icon(bd));
                        }

                    }
                });

            }else if ("event".equals(title)){
                ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Emergency");
                // 4
                eventQuery.whereEqualTo("Location", parseGeoPoint);

                // 6
                eventQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        // Handle the results
                        for (ParseObject post : objects) {
                            double lat = post.getParseGeoPoint("Location").getLatitude();
                            double lng = post.getParseGeoPoint("Location").getLongitude();

                            Drawable circle;
                            if(!"Finished".equals(post.getString("Status"))) {
                                circle = getResources().getDrawable(R.drawable.ic_marker_red);
                            }else{
                                circle = getResources().getDrawable(R.drawable.ic_marker_grey);
                            }
                            Canvas canvas = new Canvas();
                            Bitmap bitmap = Bitmap.createBitmap(circle.getIntrinsicWidth(), circle.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                            canvas.setBitmap(bitmap);
                            circle.setBounds(0, 0, circle.getIntrinsicWidth(), circle.getIntrinsicHeight());
                            circle.draw(canvas);
                            BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(bitmap);

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title("Emergency Event")
                                    .snippet(post.getString("Name"))
                                    .icon(bd));
                        }

                    }
                });

            }


            mMap.addPolyline(result);
            zoomToPoints();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


}
