package com.example.osorekoxuan.cardiact;

import android.app.Activity;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ProfileViewActivity extends ListActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static int RESULT_SELECT_IMAGE = 100;
    private TextView userName, phoneNumber, emailAddress, homeAddress,cprRegister,preferedLocation, preferedTime;
    private TextView usernameView, emailView;
    private NavigationView navigationView;
    ArrayList<ListViewItem> item = new ArrayList<>();
    ListView profileList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String name;
        ParseUser currentuser = ParseUser.getCurrentUser();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

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

        BootstrapButton mEmailSignUpButton = (BootstrapButton) findViewById(R.id.edit_profile);
        mEmailSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileViewActivity.this, ProfileManageActivity.class);
                startActivity(intent);
            }
        });

        if(ParseUser.getCurrentUser().getString("lastname") == null){
            name = ParseUser.getCurrentUser().getString("firstname");
        }else{
            name = ParseUser.getCurrentUser().getString("firstname") + " " + ParseUser.getCurrentUser().getString("lastname");
        }

        profileList = getListView();
        ListViewItem listViewItem = new ListViewItem();
        listViewItem.Title= "name";
        listViewItem.Context = name;
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title= "Phone";
        listViewItem.Context = currentuser.getString("phone");
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title= "Email";
        listViewItem.Context = currentuser.getString("email");
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title= "Address";
        listViewItem.Context = currentuser.getString("address");
        item.add(listViewItem);

        listViewItem = new ListViewItem();
        listViewItem.Title= "CPR Certified";

        if(currentuser.getBoolean("CPR") == true){
            listViewItem.Context = "Yes";
        }else{
            listViewItem.Context = "No";
        }

        item.add(listViewItem);

        setListAdapter(new ListViewAdapter(ProfileViewActivity.this, item));

        checkLoginStatus();
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
            Intent intent = new Intent(ProfileViewActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(ProfileViewActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_register) {
            Intent intent = new Intent(ProfileViewActivity.this, RegisterActivity.class);
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
            Intent intent = new Intent(ProfileViewActivity.this, ProfileViewActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_ins){
            Intent intent = new Intent(ProfileViewActivity.this, InstructionActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_contact){
            Intent intent = new Intent(ProfileViewActivity.this, ContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
