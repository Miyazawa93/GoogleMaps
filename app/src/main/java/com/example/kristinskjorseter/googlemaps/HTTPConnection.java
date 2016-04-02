package com.example.kristinskjorseter.googlemaps;

/**
 * Created by kristinskjorseter on 02/04/16.
 */
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HTTPConnection {

    public String readUrl(String mapsApiDirectionsUrl) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mapsApiDirectionsUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }
            data = stringBuffer.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception while reading", e.toString());
        } finally {
            if (inputStream != null) {
                inputStream.close();
                urlConnection.disconnect();
            }
        }
        return data;
    }



}