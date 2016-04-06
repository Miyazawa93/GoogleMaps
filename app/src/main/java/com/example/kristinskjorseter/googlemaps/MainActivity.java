package com.example.kristinskjorseter.googlemaps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public GoogleMap googleMap;
    public SharedPreferences sharedPreferences;
    private ArrayList<LatLng> arrayPoints = null;
    public int count = 0;
    public LocationManager locationManager;

    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};

    private final int MAP_TYPE_NORMAL = 0;
    private final int MAP_TYPE_HYBRID = 1;
    private final int MAP_TYPE_SATELLITE = 2;
    private final int MAP_TYPE_TERRAIN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String serviceString = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getSystemService(serviceString);

        arrayPoints = new ArrayList<LatLng>();

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = fm.getMap();

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        sharedPreferences = getSharedPreferences("location", 0);
        count = sharedPreferences.getInt("locationCount", 0);
        String zoom = sharedPreferences.getString("zoom", "0");

            if(count!=0){

                String lat = "";
                String lng = "";

                for(int i=0;i<count;i++){

                    lat = sharedPreferences.getString("lat"+i,"0");
                    lng = sharedPreferences.getString("lng"+i,"0");
                    drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
                }

                googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom)));

        }

        googleMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                count++;
                drawMarker(point);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lat" + Integer.toString((count - 1)), Double.toString(point.latitude));
                editor.putString("lng" + Integer.toString((count - 1)), Double.toString(point.longitude));
                editor.putInt("locationCount", count);
                editor.putString("zoom", Float.toString(googleMap.getCameraPosition().zoom));
                editor.commit();

                Toast.makeText(getBaseContext(), "Marker is added to the Map", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void drawMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        googleMap.addMarker(markerOptions);
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(5);
        arrayPoints.add(latLng);
        polylineOptions.addAll(arrayPoints);
        googleMap.addPolyline(polylineOptions);
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
        if(item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
    }

    public boolean onNavigationItemSelected(int itemPosition) {
        googleMap.setMapType(MAP_TYPES[itemPosition]);
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
    protected void onResume() {
        super.onResume();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
    }
    public void quitDialog(){
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

    private void clearFile() {
        googleMap.clear();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        count=0;
    }

}