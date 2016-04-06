package com.example.kristinskjorseter.googlemaps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private final String TAG = "PathGoogleMapActivity";
    private LocationManager locationManager;
    private String fileNameInternal = "positions.txt";
    private ArrayList<LatLng> arrayPoints = null;
    public Marker marker;
    public String texttosend = "";
    public int numberOfObjects;
    public SharedPreferences sharedPreferences;
    int locationCount = 0;

    //private PolylineOptions polylineOptions;

    private static final LatLng BODOE = new LatLng(67.280357, 14.404916);
    private static final LatLng KEISERVARDEN = new LatLng(67.3148173, 14.4785015);
    private static final LatLng SALTSTRAUMEN = new LatLng(67.2333, 14.6167);
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private final int MAP_TYPE_NORMAL = 0;
    private final int MAP_TYPE_HYBRID = 1;
    private final int MAP_TYPE_SATELLITE = 2;
    private final int MAP_TYPE_TERRAIN = 3;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayPoints = new ArrayList<LatLng>();
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = supportMapFragment.getMap();
        mMap.setOnMapClickListener(this);

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        String serviceString = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(serviceString);

        onMapReady(mMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.normal:
                onNavigationItemSelected(MAP_TYPE_NORMAL);
                isChecked(item);
                return true;
            case R.id.satellite:
                onNavigationItemSelected(MAP_TYPE_SATELLITE);
                isChecked(item);
                return true;
            case R.id.hybrid:
                onNavigationItemSelected(MAP_TYPE_HYBRID);
                isChecked(item);
                return true;
            case R.id.terrain:
                onNavigationItemSelected(MAP_TYPE_TERRAIN);
                isChecked(item);
                return true;
            case R.id.delete:
                deleteDialog();
                return true;
            case R.id.quit:
                quitDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void isChecked(MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
    }

    public boolean onNavigationItemSelected(int itemPosition) {
        mMap.setMapType(MAP_TYPES[itemPosition]);
        return (true);
    }

    public void deleteDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Delete all saved coordinates? ");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        clearFile();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

// Opening the sharedPreferences object
        sharedPreferences = getSharedPreferences("location", 0);

// Getting number of locations already stored
        locationCount = sharedPreferences.getInt("locationCount", 0);

// Getting stored zoom level if exists else return 0
        String zoom = sharedPreferences.getString("zoom", "0");

// If locations are already saved
        if (locationCount != 0) {

            String lat = "";
            String lng = "";

// Iterating through all the locations stored
            for (int i = 0; i < locationCount; i++) {

// Getting the latitude of the i-th location
                lat = sharedPreferences.getString("lat" + i, "0");

// Getting the longitude of the i-th location
                lng = sharedPreferences.getString("lng" + i, "0");

// Drawing marker on the map
                drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            }

        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        markerOptions.title(getAddressFromLatLng(latLng));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        mMap.addMarker(markerOptions);
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(5);
        arrayPoints.add(latLng);
        polylineOptions.addAll(arrayPoints);
        mMap.addPolyline(polylineOptions);

        writeCoordinatesToFile(latLng);

    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            return addresses.get(0).getAddressLine(0);
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
    }

    public void quitDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Quit");
        alertDialog.setMessage("Are you sure you want to quit? ");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void writeCoordinatesToFile(LatLng latLng) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putFloat("Lat" + numberOfObjects, (float) latLng.latitude);
        editor.putFloat("Lng" + numberOfObjects, (float) latLng.longitude);
        numberOfObjects++;

        editor.apply();
    }
    private ArrayList<LatLng> readCoordinatesFromFile() {
        ArrayList<LatLng> arrayList = new ArrayList<>();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        while (true) {
            float lat = sharedPref.getFloat("Lat" + numberOfObjects, 666);
            float lng = sharedPref.getFloat("Lng" + numberOfObjects, 420);

            if (lat == 666 || lng == 420)
                break;

            arrayList.add(new LatLng(lat, lng));
            numberOfObjects++;
        }

        return arrayList;
    }

    private void clearFile() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        for(;numberOfObjects >=0; numberOfObjects--) {
            editor.remove("Lat" + numberOfObjects).apply();
            editor.remove("Lng" + numberOfObjects).apply();
        }
    }

    private void drawMarker(LatLng point){
// Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

// Setting latitude and longitude for the marker
        markerOptions.position(point);

// Adding marker on the Google Map
        mMap.addMarker(markerOptions);
    }

}
