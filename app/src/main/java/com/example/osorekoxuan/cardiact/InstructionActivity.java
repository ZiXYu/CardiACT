package com.example.osorekoxuan.cardiact;


import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
public class InstructionActivity extends ListActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaPlayer.OnCompletionListener{
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private TextView usernameView, emailView;
    private NavigationView navigationView;
    private TextView aedAddress, aedLocation, aedNumber, aedZip, facilityName, facilityType, unitNumLabel, unitNum;
    private double latitude, longitude;
    public BootstrapButton pathFindingBtn, finishButton;
    private AwesomeTextView warningText;
    private String objectId;
    ArrayList<ListViewItem> item = new ArrayList<>();
    ListView InsList;

    int[] tracks = new int[4];
    int currentTrack = 0;
    private MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

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

        InsList = getListView();

        ListViewItem listViewItem;

        listViewItem = new ListViewItem();
        listViewItem.Title = "Please start CPR";
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title = "Place two fingers at the sternum (the spot where the lower ribs meet) then put the heel of your other hand next to your fingers.";
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title = "Place one hand on top of the other and interlace the fingers.";
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title = "Lock your elbows and using your body's weight, compress the victim’s chest at 5 centimeters of compression depth and at a rate of 100 to 120 beats per minute.";
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title = "Keep victim’s airway open, and allow full chest recoil between compressions.\n";
        item.add(listViewItem);

        setListAdapter(new ListViewImgAdapter(InstructionActivity.this, item));

        finishButton = (BootstrapButton) findViewById(R.id.event_finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InstructionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tracks[0] = R.raw.audio_0;
        tracks[1] = R.raw.audio_1;
        tracks[2] = R.raw.audio_2;
        tracks[3] = R.raw.audio_3;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), tracks[currentTrack]);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.start();

        checkLoginStatus();
    }

    public void onCompletion(MediaPlayer arg0) {
        arg0.release();
        if (currentTrack < tracks.length - 1) {
            currentTrack++;
            arg0 = MediaPlayer.create(getApplicationContext(), tracks[currentTrack]);
            arg0.setOnCompletionListener(this);
            arg0.start();
        }
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
            Intent intent = new Intent(InstructionActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(InstructionActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_register) {
            Intent intent = new Intent(InstructionActivity.this, RegisterActivity.class);
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
            Intent intent = new Intent(InstructionActivity.this, ProfileViewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ins){
            Intent intent = new Intent(InstructionActivity.this, InstructionActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_contact){
            Intent intent = new Intent(InstructionActivity.this, ContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
