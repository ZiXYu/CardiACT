package com.example.osorekoxuan.cardiact;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRole;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;

    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 1;

    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;
    private int mostRecentMapUpdate;
    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private String selectedPostObjectId;

    private TextView usernameView, emailView;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private GoogleMap mMap;
    Location mLastLocation, mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double lat,lon;
    private ParseQueryAdapter<AED> aedParseQueryAdapter;
    ParseRole role;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("Roles", "Bystander");
        installation.saveInBackground();

        radius = Application.getSearchDistance();
        lastRadius = radius;

        //enable bootstrap
        TypefaceProvider.registerDefaultIconSets();

        buildGoogleApiClient();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        checkLoginStatus();
    }
//    @Override
//    public void onResume(){
//        if(Helper.isPathDrawn == false);{
//            Toast.makeText(getApplicationContext(),"Drawing path", Toast.LENGTH_SHORT).show();
//            Helper.isPathDrawn = true;
//        }
//    }
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
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_register) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
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
            Intent intent = new Intent(MainActivity.this, ProfileViewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ins){
            Intent intent = new Intent(MainActivity.this, InstructionActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_contact){
            Intent intent = new Intent(MainActivity.this, ContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkLoginStatus(){
        Menu menuNav = navigationView.getMenu();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            String name = currentUser.getString("firstname") + " " + currentUser.getString("lastname");
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


    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
//            if(Helper.isPathDrawn == false){
//                Toast.makeText(getApplicationContext(),"Drawing path", Toast.LENGTH_SHORT).show();
//                Helper.isPathDrawn = true;
//        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        // Set up the camera change handler
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                // Run the map query
                doMapQuery();
            }
        });
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every second
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
                LatLng latLng = new LatLng(lat, lon);
                CameraUpdate center= CameraUpdateFactory.newLatLng(latLng);
                CameraUpdate zoom= CameraUpdateFactory.zoomTo(14);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }
        }
        doMapQuery();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    private void doMapQuery() {
        // 1
        Location myLoc = new Location("My Location");
         myLoc = (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;

        if(myLoc == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Sorry, your location information could not be accessed!")
                    .setNegativeButton("Cancel", null);
            builder.show();
            return;
        }
        // 2
        final ParseGeoPoint myPoint = new ParseGeoPoint(myLoc.getLatitude(), myLoc.getLongitude());
        // 3
        ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery("AED_DATA");
        // 4
        mapQuery.whereWithinKilometers("LOCATION", myPoint, 1);
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


                    double distance = myPoint.distanceInKilometersTo(post.getParseGeoPoint("LOCATION"));

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title("AED")
                            .snippet(post.getString("ADD_FULL"))
                            .icon(bd));
                }

            }
        });

        ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Emergency");
        // 4
        eventQuery.whereWithinKilometers("Location", myPoint, 1);

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

    @Override
    public void onInfoWindowClick(final Marker marker) {

        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        Intent intent;
        if("AED".equals(marker.getTitle())){
            intent = new Intent(MainActivity.this, AEDActivity.class);
            Bundle b = new Bundle();
            b.putDouble("latitude", latitude); //Your id
            b.putDouble("longitude", longitude); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
        }else{
            intent = new Intent(MainActivity.this, EventDetailActivity.class);
            Bundle b = new Bundle();
            b.putDouble("latitude", latitude); //Your id
            b.putDouble("longitude", longitude); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
        }
        Log.d("My Map Click", "Click Lisener Successfully");
    }
}
