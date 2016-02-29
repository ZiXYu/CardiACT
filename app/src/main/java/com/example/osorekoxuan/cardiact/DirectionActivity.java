package com.example.osorekoxuan.cardiact;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * Created by JINGNAN on 2016-02-29.
 */
public class DirectionActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    LatLng myLatLng;
    LatLng shopLatLng;
    private double des_latitude, des_longitude, mylat, mylng;
    Boolean isDirectionDrawn = false;
    private GoogleMap mMap;
    public LocationManager locationManager;
    private String gpsProvider, netProvider;
    private String DEBUGTAG = "Direction Activity: ";
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                mylat = location.getLatitude();
                mylng = location.getLongitude();
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
        Bundle b = getIntent().getExtras();
        des_latitude = b.getDouble("latitude");
        des_longitude = b.getDouble("longitude");
        getPosition();

    }

    public void getPosition() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gpsProvider = LocationManager.GPS_PROVIDER;
        if (locationManager.isProviderEnabled(gpsProvider)) {
            Location location = locationManager.getLastKnownLocation(gpsProvider);
            if (location != null) {
                mylat = location.getLatitude();
                mylng = location.getLongitude();
                myLatLng = new LatLng(mylat, mylng);
            }
        } else {
            netProvider = LocationManager.NETWORK_PROVIDER;
            locationManager.requestLocationUpdates(netProvider, 1000, 0, locationListener);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location1 != null) {
                mylat = location1.getLatitude();
                mylng = location1.getLongitude();
                myLatLng = new LatLng(mylat, mylng);
            }
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

        mMap.setMyLocationEnabled(true);

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

            mMap.addMarker(new MarkerOptions()
                            .position(shopLatLng)
                            .title("Destination")
            );

            mMap.addPolyline(result);
            zoomToPoints();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


}
