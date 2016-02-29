//package com.example.osorekoxuan.cardiact;
//
//import android.content.Context;
//import android.content.res.AssetManager;
//import android.graphics.Color;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.os.AsyncTask;
//import android.support.v4.app.FragmentActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.android.gms.maps.CameraUpdate;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.PolylineOptions;
//import com.opencsv.CSVReader;
//import com.parse.ParseGeoPoint;
//import com.parse.ParseObject;
//
//import org.w3c.dom.Document;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
//
//    LatLng myLatLng;
//    LatLng shopLatLng;
//    Boolean isDirectionDrawn = false;
//    private GoogleMap mMap;
//    LocationManager locationManager;
//    Criteria criteria;
//    final String DEBUGTAG = "Maps Activity: ";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        /*List<String[]> aedData = readCsv(this);
//
//        for (int i=750; i<aedData.size(); i++){
//            String[] AED= aedData.get(i);
//            ParseObject aed_data = new ParseObject("AED_DATA");
//            aed_data.put("ADD_FULL", AED[0]);
//            aed_data.put("POSTAL_CD", AED[1]);
//            aed_data.put("X", AED[2]);
//            aed_data.put("Y", AED[3]);
//            ParseGeoPoint point = new ParseGeoPoint(Double.parseDouble(AED[4]), Double.parseDouble(AED[5]));
//            aed_data.put("LOCATION", point);
//            aed_data.put("OBJECTID", AED[6]);
//            aed_data.put("FACI_NAM", AED[7]);
//            aed_data.put("FACI_TYPE", AED[8]);
//            aed_data.put("UNIT", AED[9]);
//            aed_data.put("UNIT_TYPE", AED[10]);
//            aed_data.put("DEV_LOC", AED[11]);
//            aed_data.put("DEV_COUNT", AED[12]);
//            aed_data.saveInBackground();
//        }*/
//    }
//
//    private final LocationListener gpsLocationListener =new LocationListener(){
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            switch (status) {
//                case LocationProvider.AVAILABLE:
//                    Toast.makeText(getApplicationContext(), "GPS available again", Toast.LENGTH_SHORT).show();
//                    break;
//                case LocationProvider.OUT_OF_SERVICE:
//                    Toast.makeText(getApplicationContext(), "GPS out of service", Toast.LENGTH_SHORT).show();
//                    break;
//                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    Toast.makeText(getApplicationContext(), "GPS temporarily unavailable", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            Log.d(DEBUGTAG, "GPS Provider Enabled\n");
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            Log.d(DEBUGTAG, "GPS Provider Disabled\n");
//        }
//
//        @Override
//        public void onLocationChanged(Location location) {
//            locationManager.removeUpdates(networkLocationListener);
//            Log.d(DEBUGTAG,"New GPS location: "
//                    + String.format("%9.6f", location.getLatitude()) + ", "
//                    + String.format("%9.6f", location.getLongitude()) + "\n");
//        }
//    };
//    private final LocationListener networkLocationListener =
//            new LocationListener(){
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras){
//                    switch (status) {
//                        case LocationProvider.AVAILABLE:
//                            Toast.makeText(getApplicationContext(), "Network location available again", Toast.LENGTH_SHORT).show();
//                            break;
//                        case LocationProvider.OUT_OF_SERVICE:
//                            Toast.makeText(getApplicationContext(), "Network location out of service", Toast.LENGTH_SHORT).show();
//                            break;
//                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                            Toast.makeText(getApplicationContext(), "Network location temporarily unavailable", Toast.LENGTH_SHORT).show();
//                            break;
//                    }
//                }
//
//                @Override
//                public void onProviderEnabled(String provider) {
//                    Log.d(DEBUGTAG, "Network Provider Enabled\n");
//                }
//
//                @Override
//                public void onProviderDisabled(String provider) {
//                    Log.d(DEBUGTAG, "Network Provider Disabled\n");
//                }
//
//                @Override
//                public void onLocationChanged(Location location) {
//                    Log.d(DEBUGTAG, "New network location: "
//                            + String.format("%9.6f", location.getLatitude()) + ", "
//                            + String.format("%9.6f", location.getLongitude()) + "\n");
//                }
//            };
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        locationManager.requestLocationUpdates(
//                LocationManager.NETWORK_PROVIDER, 5000, 0,
//                networkLocationListener);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                3000, 0, gpsLocationListener);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        locationManager.removeUpdates(networkLocationListener);
//        locationManager.removeUpdates(gpsLocationListener);
//    }
//
//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        // for testing
//        double latitude = 43.6617;
//        double longitude = -79.3950;
//        shopLatLng = new LatLng(latitude, longitude);
//        // Add a marker in Sydney and move the camera
//        mMap.addMarker(new MarkerOptions()
//                        .position(shopLatLng)
//                        .title("u of t")
//        );
//        Location location = locationManager.getLastKnownLocation(locationManager
//                .getBestProvider(criteria, false));
//
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, gpsLocationListener);
//        mMap.setMyLocationEnabled(true);
//        myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//        if(!isDirectionDrawn) {
//            new LongOperation().execute("");
//        }
//    }
//
//    private void zoomToPoints() {
//        try {
//
//
//            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//            //        for (Marker marker : markers) {
//            builder.include(myLatLng);
//            builder.include(shopLatLng);
//            //        }
//            LatLngBounds bounds = builder.build();
//
//            int padding = 50; // offset from edges of the map in pixels
//            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//
//            mMap.animateCamera(cu);
//        }
//        catch (Exception e)
//        {
//            ///possible error:
//            /// java.lang.NullPointerException: Attempt to invoke interface method 'org.w3c.dom.NodeList org.w3c.dom.Document.getElementsByTagName(java.lang.String)' on a null object reference
//        }
//    }
//    private class LongOperation extends AsyncTask<String, Void, PolylineOptions> {
//
//        private PolylineOptions getDirection() {
//            try {
//                GMapV2Direction md = new GMapV2Direction();
//
//                Document doc = md.getDocument(myLatLng, shopLatLng,
//                        GMapV2Direction.MODE_WALKING);
//
//                ArrayList<LatLng> directionPoint = md.getDirection(doc);
//                PolylineOptions rectLine = new PolylineOptions().width(9).color(
//                        Color.GREEN);
//
//                for (int i = 0; i < directionPoint.size(); i++) {
//                    rectLine.add(directionPoint.get(i));
//                }
//                isDirectionDrawn = true;
//
//                return rectLine;
//            }
//            catch (Exception e)
//            {
//                ///possible error:
//                ///java.lang.IllegalStateException: Error using newLatLngBounds(LatLngBounds, int): Map size can't be 0. Most likely, layout has not yet occured for the map view.  Either wait until layout has occurred or use newLatLngBounds(LatLngBounds, int, int, int) which allows you to specify the map's dimensions.
//                return null;
//            }
//
//        }
//
//        @Override
//        protected PolylineOptions doInBackground(String... params) {
//            PolylineOptions polylineOptions = null;
//            try {
//                polylineOptions = getDirection();
//            } catch (Exception e) {
//                Thread.interrupted();
//            }
//            return polylineOptions;
//        }
//
//        @Override
//        protected void onPostExecute(PolylineOptions result) {
//            // might want to change "executed" for the returned string passed
//            // into onPostExecute() but that is upto you
//
//            mMap.clear();///TODO: clean the path only.
//
//            mMap.addMarker(new MarkerOptions()
//                            .position(shopLatLng)
//                            .title("Lou Lan Cha")
//            );
//
//            mMap.addPolyline(result);
//            zoomToPoints();
//        }
//
//        @Override
//        protected void onPreExecute() {}
//
//        @Override
//        protected void onProgressUpdate(Void... values) {}
//    }
//    public final List<String[]> readCsv(Context context) {
//        List<String[]> questionList = new ArrayList<String[]>();
//        AssetManager assetManager = context.getAssets();
//
//        try {
//            InputStream csvStream = assetManager.open("AED_DATA.csv");
//            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
//            CSVReader csvReader = new CSVReader(csvStreamReader);
//            String[] line;
//
//            // throw away the header
//            csvReader.readNext();
//
//            while ((line = csvReader.readNext()) != null) {
//                questionList.add(line);
//                Log.d("AED", line[0]);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return questionList;
//    }
//}
