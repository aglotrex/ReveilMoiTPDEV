package com.trigg.alarmclock;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OutilsLocalisation implements LocationListener {
    private final String API_KAY = "a55b9cc2a91ba613f531b9c6b1871235";

    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    private Main_test main_test;

    public static String TEMP = "n";
    public static String CITY = "0";

    public OutilsLocalisation(Main_test main_test){
        this.main_test = main_test;
    }
    public static String getCITY() {
        return CITY;
    }

    public static String getTEMP() {
        return TEMP;
    }

    @Override
    public void onLocationChanged(Location location) {
        TEMP += "n";
        CITY = TEMP.length() + "";
        /**if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            accuracy = location.getAccuracy();

            checkWeather(location);
        }
       else {
            if (latitude ==0.0){
                latitude = 48.866667;
            }
            if (longitude ==0.0){
                longitude = 2.333333;
            }
            checkWeather(null);
        }**/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        String newStatus = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                newStatus = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                newStatus = "TEMPORARILY_UNAVAILABLE";
                break;
            case LocationProvider.AVAILABLE:
                newStatus = "AVAILABLE";
                break;
        }
        String msg = String.format(main_test.getResources().getString(R.string.provider_new_status), provider, newStatus);
        Toast.makeText(main_test, msg, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onProviderEnabled(String provider) {

        String msg = String.format(main_test.getResources().getString(R.string.provider_enabled), provider);
        Toast.makeText(main_test, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {

        String msg = String.format(main_test.getResources().getString(R.string.provider_disabled), provider);
        Toast.makeText(main_test, msg, Toast.LENGTH_SHORT).show();

    }

    public void checkWeather(Location loc) {


        try {

            JSONObject weather = getInfo("http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" +longitude + "&units=metric&appid=" + API_KAY);

            CITY = weather.getString("name");
            JSONObject main = weather.getJSONObject("main");

            TEMP = main.getDouble("temp") + "°C";
            String HUMIDITI = main.getString("humidity") + " %";

            JSONObject wind = weather.getJSONObject("wind");
            String WINDSPEED = wind.getInt("speed") + " m/s";


            TextView t =  main_test.findViewById(R.id.temperature);
            t.setText(TEMP);

            ((TextView) main_test.findViewById(R.id.city)).setText(CITY);

            t =  main_test.findViewById(R.id.wind);
            t.setText(WINDSPEED);

            t =  main_test.findViewById(R.id.humidity);
            t.setText(HUMIDITI);

        } catch (JSONException e) {
            TextView t =  main_test.findViewById(R.id.temperature);
            t.setText("N/A");

            ((TextView) main_test.findViewById(R.id.city)).setText("N/A");

            t = main_test.findViewById(R.id.wind);
            t.setText("N/A");

            t = main_test.findViewById(R.id.humidity);
            t.setText("N/A");

        }

    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    // Recupere un JSON a partir d'une url
    public JSONObject getInfo(String addr) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url = null;
        HttpURLConnection urlConnection = null;
        String result = null;
        try {
            url = new URL(addr);
            urlConnection = (HttpURLConnection) url.openConnection(); // Open
            InputStream in = new BufferedInputStream(urlConnection.getInputStream()); // Stream

            result = readStream(in); // Read stream
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        JSONObject json = null;
        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return json; // returns the result
    }
}
