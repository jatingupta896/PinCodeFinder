package com.example.jatin.AddressLocator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private String location;
    private boolean currentLocation;
    private Geocoder geocoder;
    private TextView pincode;
    private boolean mPermissionDenied = false;
    int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        pincode = (TextView) findViewById(R.id.pincode);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        location = intent.getStringExtra("Location");
        flag= Integer.parseInt(intent.getStringExtra("Flag"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        List<Address> addressList = null;

        if (location != null || !location.equals("") && geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this);
//            if (addressList == null || addressList.size() == 0) {
//                Snackbar snackbar = Snackbar
//                        .make(findViewById(R.id.map), "Invalid Address or address is not found" + location, Snackbar.LENGTH_SHORT);
//
//                snackbar.show();
//                startActivity(new Intent(this, MainActivity.class));
//            } else {
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

            mMap.getMaxZoomLevel();
            double lattitude = address.getLatitude();
            double longitude = address.getLongitude();
            if (address.getPostalCode() == null) {
                pincode.setText("Unable to find, Please modify your Address");
            } else {
                pincode.setText(address.getPostalCode());
            }
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.map), " Lattitude: " + lattitude + " Longitude: " + longitude + " Address: " + location, Snackbar.LENGTH_SHORT);

            snackbar.show();
            //}
            // Toast.makeText(this, " Lattitude: " + lattitude + " Longitude: " + longitude + " Address: " + location, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Invalid Address", Toast.LENGTH_SHORT).show();
        }

        enableMyLocation();

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
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

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startActivity(new Intent(this,MainActivity.class));
//    }
}
