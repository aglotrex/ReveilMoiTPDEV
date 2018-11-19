package com.trigg.alarmclock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;


public class Main_test extends Activity {

    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;


    // Gestion de la position
    private LocationManager lm;
    private static OutilsLocalisation outilsLOC;
    // Attributs pour sauvegarder la position de l'utilisateur

    private Boolean checkDone = false;

    public static String getCITY() {
        return outilsLOC.getCITY();
    }

    public static String getTEMP() {
        return outilsLOC.getTEMP();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Autorise la bar d'action dans le menu
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setTitle("Reveil moi !");

        // Set en fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set le layout
        setContentView(R.layout.main_test_activity);
        setTextViewColorFromFrameLayout(Color.WHITE, findViewById(R.id.mainLayout));
        if (outilsLOC == null)
            outilsLOC = new OutilsLocalisation(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Display le menu
        getMenuInflater().inflate(R.menu.main_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Action lors du click sur la bar d'action
        switch (item.getItemId()) {
            // Action a effectue lors du click sur ajouter une alarme
            case R.id.action_add_new_alarm: {
                startAlarmDetailsActivity(-1);
                break;
            }
            case R.id.action_list: {
                Intent intent = new Intent(this, AlarmListActivity.class);
                startActivity(intent);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // Ouvre l'intent de modification de l'alarme (pareil que celui de creation)
    public void startAlarmDetailsActivity(long id) {
        Intent intent = new Intent(this, AlarmDetailsActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, 0);
    }


    protected static void setTextViewColorFromFrameLayout(int color, View v) {
        if (v instanceof FrameLayout) {
            FrameLayout r = (FrameLayout) v;
            for (int i = 0; i < r.getChildCount(); i++)
                if (r.getChildAt(i) instanceof TextView)
                    ((TextView) r.getChildAt(i)).setTextColor(color);
        }
    }


    // Methode necessaire a l'utilisation du GPS

    // Lors du lancement de l'appli on "s'abonne" aux MAJ des coords de l'utilisateur
    @Override
    protected void onResume() {
        super.onResume();

        if (!checkDone) {
            checkTTS();
        }
        // Recuperation du service de localisation

        // Construction des criteres pour le bon provider
        Criteria criteria = new Criteria();
        // Demande d'une bonne precisino
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // Recuperation du meilleur provider (si GPS active c'est le GPS qui est pris en general)
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = lm.getBestProvider(criteria, true);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        lm.requestLocationUpdates(provider, 0, 10, outilsLOC);
        outilsLOC.checkWeather(lm.getLastKnownLocation(provider));
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new
                                    String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                        } else {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
                        }
                    }
                }
                break;
            case 2:

                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            Criteria criteria = new Criteria();
                            // Demande d'une bonne precisino
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);

                            String provider = lm.getBestProvider(criteria, true);
                            lm.requestLocationUpdates(provider, 0, 10, outilsLOC);
                            outilsLOC.checkWeather(lm.getLastKnownLocation(provider));

                        } else {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
                        }
                    }
                }
                break;
        }

    }



    // Ici, on s'y "desabonne"
    @Override
    protected void onPause() {
        super.onPause();
        checkDone = false;
        if (lm != null && outilsLOC != null)
            lm.removeUpdates(outilsLOC);
    }

    // Lorsque la position de l'utilisateur va etre modifie, cette methode est appellee





    // Methodes necessaires au TTS

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, AlarmScreen.CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == AlarmScreen.CHECK_CODE && !checkDone){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                AlarmScreen.speaker = new Speaker(this);
            }
            checkDone = true;
        }
    }

}
