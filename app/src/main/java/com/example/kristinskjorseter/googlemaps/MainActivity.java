package com.example.kristinskjorseter.googlemaps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,  GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    final String TAG = "PathGoogleMapActivity";
    private LocationManager locationManager;

    private String fileNameInternal = "positions.txt";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_google_map);

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
        if(item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
    }

    public boolean onNavigationItemSelected(int itemPosition) {
        mMap.setMapType(MAP_TYPES[itemPosition]);
        return (true);
    }

    public void deleteDialog(){
    //TODO: delete data from file.
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(BODOE);
        markerOptions.position(KEISERVARDEN);
        markerOptions.position(SALTSTRAUMEN);
        mMap.addMarker(markerOptions);

        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BODOE, 13));
        googleMap.addMarker(new MarkerOptions().position(BODOE).title("Bodø"));
        googleMap.addMarker(new MarkerOptions().position(KEISERVARDEN).title("Keiservarden"));
        googleMap.addMarker(new MarkerOptions().position(SALTSTRAUMEN).title("Saltstraumen"));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        markerOptions.title(getAddressFromLatLng(latLng));

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        mMap.addMarker(markerOptions);
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
            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
    }
    private String getMapsApiDirectionsUrl() {
        String waypoints = "waypoints=optimize:true|"
                + BODOE.latitude + "," + BODOE.longitude
                + "|" + "|" + KEISERVARDEN.latitude + ","
                + KEISERVARDEN.longitude + "|" + SALTSTRAUMEN.latitude + ","
                + SALTSTRAUMEN.longitude;

        String sensor = "sensor=false";
        String origin = "origin= + BODOE.latitude + “,” + BODOE.longitude;";
        String destination = "destination=" + SALTSTRAUMEN.latitude + "," + SALTSTRAUMEN.longitude;
        String params = origin + "&" + destination + "&" + waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=17.449797,78.373037&destination=17.47989,78.390095&%20waypoints=optimize:true|17.449797,78.373037||17.47989,78.390095&sensor=false"
                + output + "?" + params;
        return url;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HTTPConnection http = new HTTPConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();

            for (int i = 0; i < jsonData.length; i++) {
                try {
                    jObject = new JSONObject(jsonData[0]);
                    PathJSONParser parser = new PathJSONParser();
                    routes = parser.parse(jObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            if (routes.size() < 1) {
                Toast.makeText(MainActivity.this, "No Points", Toast.LENGTH_LONG).show();
                return;
            }

            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            mMap.addPolyline(polyLineOptions);
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

    public void doWriteAppFile(View view) {
        EditText etInput = (EditText) findViewById(R.id.etInput);
        String content = etInput.getText().toString();
        FileOutputStream fOut;
        try {
            fOut = this.getApplicationContext().openFileOutput(fileNameInternal, Context.MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(content);
            osw.close();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doReadAppFile(View view) {
        Resources res = this.getResources();
        FileInputStream fIn;
        try {
            fIn = this.getApplicationContext().openFileInput(fileNameInternal);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            TextView tvOutput = (TextView)findViewById(R.id.tvOutput); tvOutput.setText(line);
            isr.close();
            reader.close();
            fIn.close();

        } catch (FileNotFoundException e) {
        e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

