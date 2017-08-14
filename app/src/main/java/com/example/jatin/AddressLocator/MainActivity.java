package com.example.jatin.AddressLocator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity implements NetworkConnectivityReceiver.ConnectivityReceiverListener {
    private GoogleMap mMap;

    private EditText locality;
    private EditText area;
    private EditText city;
    private EditText state;
    private EditText country;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private Button submitButton;
    String lat = "", lon = "";
    private String location;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.INTERNET, true);
        } else if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_NETWORK_STATE, true);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Address");
        submitButton = (Button) findViewById(R.id.submit);
        locality = (EditText) findViewById(R.id.locality);
        area = (EditText) findViewById(R.id.area_street);
        city = (EditText) findViewById(R.id.city);
        state = (EditText) findViewById(R.id.state);
        country = (EditText) findViewById(R.id.country);

        SharedPreferences sharedPreferences = getSharedPreferences("DemoFile", 0);
        String sr = sharedPreferences.getString("STATUS1", null);
        locality.setText(sr);
        String sl = sharedPreferences.getString("STATUS2", null);
        area.setText(sl);
        String sc = sharedPreferences.getString("STATUS3", null);
        city.setText(sc);
        String ss = sharedPreferences.getString("STATUS4", null);
        state.setText(ss);
        String sco = sharedPreferences.getString("STATUS5", null);
        country.setText(sco);
        String sz = sharedPreferences.getString("STATUS6", null);

    }

    public void findPincode(View view) {
        if (isConnected(this)) {
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            String Locality = locality.getText().toString().trim();
            String Area = area.getText().toString().trim();
            String City = city.getText().toString().trim();
            String State = state.getText().toString().trim();
            String Country = country.getText().toString().trim();
            if (TextUtils.isEmpty(Locality)) {
                Locality = " ";
            }
            if (TextUtils.isEmpty(Area)) {
                Toast.makeText(MainActivity.this, "Area and Street cannot be empty.", Toast.LENGTH_SHORT).show();
            }

            if (City == "") {
                Toast.makeText(MainActivity.this, "City cannot be empty.", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(State)) {
                Toast.makeText(MainActivity.this, "State cannot be empty.", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(Country)) {
                Toast.makeText(MainActivity.this, "Country cannot be empty.", Toast.LENGTH_SHORT).show();
            }

            if (!TextUtils.isEmpty(Locality) && !TextUtils.isEmpty(Area) && !TextUtils.isEmpty(City) && !TextUtils.isEmpty(State) && !TextUtils.isEmpty(Country)) {
                progressDialog.show();
                DatabaseReference post = databaseReference.push();
                post.child("Area").setValue(Area);
                post.child("Locality").setValue(Locality);
                post.child("City").setValue(City);
                post.child("State").setValue(State);
                post.child("Country").setValue(Country);
                progressDialog.dismiss();
                location = Locality + ", " + Area + ", " + City + ", " + State + ", " + Country;
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("Location", location);
                intent.putExtra("Flag","1");
                startActivityForResult(intent, 7777);
            } else {
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.lay1), "Please fill mandatory fields", Snackbar.LENGTH_LONG);

                snackbar.show();
            }
        } else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.lay1), "Check your Internet Connection", Snackbar.LENGTH_LONG);

            snackbar.show();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("DemoFile", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("STATUS1", locality.getText().toString());
        editor.putString("STATUS2", area.getText().toString());
        editor.putString("STATUS3", city.getText().toString());
        editor.putString("STATUS4", state.getText().toString());
        editor.putString("STATUS5", country.getText().toString());
        editor.commit();
    }


    public void currentLocation(View view) {
        if (isConnected(this)) {
            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location loc = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(criteria, false));
            double latitude = loc.getLatitude();
            double longitude = loc.getLongitude();
            location = latitude + "," + longitude;
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("Location", location);
            intent.putExtra("Flag","2");
            startActivityForResult(intent, 7777);
        } else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.lay1), "Check your Internet Connection", Snackbar.LENGTH_LONG);

            snackbar.show();
        }
    }

    //Internet connectivity checking also implement NetworkConnectivityReceiver.ConnectivityReceiverListener
    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        NetworkChecking.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else
            return false;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        if (isConnected) {
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.lay1), "Internet is connected", Snackbar.LENGTH_LONG);

            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.lay1), "Check your Internet Connection", Snackbar.LENGTH_LONG);

            snackbar.show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.

        } else if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_NETWORK_STATE)) {
            // Enable the my location layer if the permission has been granted.

        } else if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.INTERNET)) {
            // Enable the my location layer if the permission has been granted.

        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;

        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}


