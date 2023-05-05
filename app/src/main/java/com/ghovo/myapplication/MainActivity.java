package com.ghovo.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    public List<String> citiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a reference to the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if the app has permission to access the device's location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request permission to access the device's location
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

        } else {

            Log.i("VE", "Getting current location");
            getCurrentLocation();
            Log.i("VE", "Current location retrieved");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the permission request was granted
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // If the permission was granted, get the user's current location
                getCurrentLocation();
            } else {

                // If the permission was not granted, show a message to the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {

        // Check if the app has permission to access the device's location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // If the app has permission to access the device's location,
            // request periodic updates of the device's location
            fusedLocationClient.requestLocationUpdates(new LocationRequest(),
                    new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // Called when the device's location changes
                            if (locationResult != null) {
                                // Use the location object to get the user's current latitude and longitude
                                double latitude = locationResult.getLastLocation().getLatitude();
                                double longitude = locationResult.getLastLocation().getLongitude();

                                // Call the method to convert the coordinates to a city name
                                String cityName = getCityName(latitude, longitude);

                                // Log the city name
                                Log.i("VERJAPES","City name: " + cityName);

                                addDatabaseToFirestore(cityName);
                                // Remove the location updates to save battery
                                fusedLocationClient.removeLocationUpdates(this);
                            }
                        }
                    }, null);
        }
    }

    private String getCityName(double latitude, double longitude) {

        // Use the Geocoder class to convert the coordinates to a city name
        String cityName = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                cityName = address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }
    public void addDatabaseToFirestore(String cityName){
        FirebaseFirestore data = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put("Username","username");
        user.put("Password","password");
        user.put("Phone Number","Phone number");
        data.collection(cityName)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Data inserted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}