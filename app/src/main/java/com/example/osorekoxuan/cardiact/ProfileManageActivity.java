package com.example.osorekoxuan.cardiact;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.parse.ParseUser;

import static android.Manifest.permission.READ_CONTACTS;

public class ProfileManageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_READ_CONTACTS = 0;

    private TextView usernameView, emailView;
    private NavigationView navigationView;
    private AutoCompleteTextView mEmailView;
    private EditText mFirstnameView, mLastnameView, phoneNumber, homeAddress;
    private CheckBox cprRegister;
    private Spinner locationPreferred;
    private TimePicker timeFromView, timeToView;
    private View mProgressView;
    private View mRegisterFormView;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        currentUser = ParseUser.getCurrentUser();

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mFirstnameView = (EditText) findViewById(R.id.first_name);
        mLastnameView = (EditText) findViewById(R.id.last_name);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        homeAddress = (EditText) findViewById(R.id.home_address);
        cprRegister = (CheckBox) findViewById(R.id.cpr_register);

        mEmailView.setText(currentUser.getEmail());
        mFirstnameView.setText(currentUser.getString("firstname"));
        mLastnameView.setText(currentUser.getString("lastname"));
        phoneNumber.setText(currentUser.getString("phone"));
        homeAddress.setText(currentUser.getString("address"));

        if(currentUser.getBoolean("CPR") == true) {
            cprRegister.setChecked(true);
        }else{
            cprRegister.setChecked(false);
        }

        BootstrapButton profileSubmit = (BootstrapButton) findViewById(R.id.profile_submit);
        profileSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSubmit();
            }
        });

        checkLoginStatus();
    }

    private void attemptSubmit() {
        String email = mEmailView.getText().toString();
        String firstname = mFirstnameView.getText().toString();
        String lastname = mLastnameView.getText().toString();
        String phone = phoneNumber.getText().toString();
        String address = homeAddress.getText().toString();
        Boolean cpr = cprRegister.isChecked();

        currentUser.put("firstname", firstname);
        currentUser.put("lastname", lastname);
        currentUser.put("phone", phone);
        currentUser.put("address", address);
        currentUser.put("CPR", cpr);
        currentUser.saveInBackground();

        Toast.makeText(ProfileManageActivity.this, "Update Profile Successfully", Toast.LENGTH_LONG).show();
        Log.d("Profile", "Update Successfully!");
        Intent intent = new Intent(ProfileManageActivity.this, ProfileViewActivity.class);
        startActivity(intent);
        finish();
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

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
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
            Intent intent = new Intent(ProfileManageActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(ProfileManageActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_register) {
            Intent intent = new Intent(ProfileManageActivity.this, RegisterActivity.class);
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
            Intent intent = new Intent(ProfileManageActivity.this, ProfileViewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_ins){
            Intent intent = new Intent(ProfileManageActivity.this, InstructionActivity.class);
            startActivity(intent);
        }  else if (id == R.id.nav_contact){
            Intent intent = new Intent(ProfileManageActivity.this, ContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
