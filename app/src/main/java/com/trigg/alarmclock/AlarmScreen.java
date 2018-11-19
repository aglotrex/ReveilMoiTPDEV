package com.trigg.alarmclock;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class declenchant l'ecran de wake up de l'alarme
 */
public class AlarmScreen extends Activity {
	
	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;

    // Attributs pour le TTS
    protected static final int CHECK_CODE = 0;
    private final int LONG_DURATION = 3000;
    private final int SHORT_DURATION = 1000;
    private final int VERY_SHORT_DURATION = 500;
    protected static Speaker speaker;
    private String number;
    // Pour les heures
    private int timeHour;
    private int timeMinute;

    // Pour la musique
    private String tone;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("unchecked")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set le layout
		this.setContentView(R.layout.activity_alarm_screen);

        // Recuperation des informations sur l'alarme
		String name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
		timeHour = getIntent().getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
		timeMinute = getIntent().getIntExtra(AlarmManagerHelper.TIME_MINUTE, 0);
		tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);


        // Creation de la vue a partir des donnees recuperees
		TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
		tvName.setText(name);
		
		//TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
		//tvTime.setText(String.format("%02d : %02d", timeHour, timeMinute));

        // Mise en place des couleurs
        FrameLayout r = (FrameLayout)findViewById(R.id.alarmScreenLayout);
        for( int i = 0; i < r.getChildCount(); i++ )
            if( r.getChildAt( i ) instanceof TextView)
                ((TextView) r.getChildAt( i )).setTextColor(Color.WHITE);

        // Mis en place du bouton pour stopper l'alarme
		Button dismissButton = (Button) findViewById(R.id.alarm_screen_button);
		dismissButton.setOnClickListener(new OnClickListener() {
			// Si click, stopper la musique et couper l'ecran
			@Override
			public void onClick(View view) {
                speaker.onDestroy();
				if(mPlayer != null && mPlayer.isPlaying())
                    mPlayer.stop();
                mPlayer.release();
				finish();
			}
		});

		// Jouer la sonnerie
		mPlayer = new MediaPlayer();
		try {
			if (tone != null && !tone.equals("")) {
				Uri toneUri = Uri.parse(tone);
				if (toneUri != null) {
					mPlayer.setDataSource(this, toneUri);
					mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					mPlayer.setLooping(true);
					mPlayer.prepare();
					mPlayer.start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Ensure wakelock release
		Runnable releaseWakelock = new Runnable() {

			@Override
			public void run() {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

				if (mWakeLock != null && mWakeLock.isHeld()) {
					mWakeLock.release();
				}
			}
		};

		new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		super.onResume();

		// Set the window to keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Acquire wakelock
		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			Log.i(TAG, "Wakelock aquired!!");
		}
        // Lancement du TTS
        if(speaker == null)
            Log.i(TAG, "NULLLLLL HIHI");
        playVoice();
	}
    @SuppressWarnings("unchecked")
    private void playVoice() {
        speaker.pause(LONG_DURATION);
        speaker.speak("Bonjour");
        speaker.pause(VERY_SHORT_DURATION);
        speaker.speak("Il est " + timeHour + " heures et " + timeMinute + " minutes." );
        speaker.pause(SHORT_DURATION);
        if(Main_test.getTEMP() == null)
            speaker.speak("La température est inconnue");
        if(Main_test.getCITY() == null)
            speaker.speak("La ville est inconnue");
        if(Main_test.getCITY() != null && Main_test.getTEMP() != null)
            speaker.speak("Il fait actuellement " + Main_test.getTEMP() + " degrés à " + Main_test.getCITY() );
        if (Check_READ_SMS(this)){
            List<String> lectureSms = getNumberUnreadSMS();
            for (String msg : lectureSms){
                speaker.speak(msg);
                speaker.pause(SHORT_DURATION);
            }
        }
        else {
            Request_READ_SMS(this, 1);
        }


    }
    public static void Request_READ_SMS(Activity act,int code)
    {
        ActivityCompat.requestPermissions(act, new
                String[]{Manifest.permission.READ_SMS},code);
    }
    public static boolean Check_READ_SMS(Activity act)
    {
        int result = ContextCompat.checkSelfPermission(act, Manifest.permission.READ_SMS);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    public static void Request_READ_CONTACTS(Activity act,int code)
    {
        ActivityCompat.requestPermissions(act, new
                String[]{Manifest.permission.READ_CONTACTS},code);
    }
    public static boolean Check_READ_CONTACTS(Activity act)
    {
        int result = ContextCompat.checkSelfPermission(act, Manifest.permission.READ_CONTACTS);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if(Check_READ_SMS(this) ) {
                        Request_READ_CONTACTS(this,2);

                    }

                }
                break;
                case 2:
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                            if(Check_READ_SMS(this) &&Check_READ_CONTACTS(this)  ) {

                                List<String> lectureSms = getNumberUnreadSMS();
                                for (String msg : lectureSms){
                                    speaker.speak(msg);
                                    speaker.pause(SHORT_DURATION);
                                }
                            }



                    }
                return;
        }
            // other 'case' lines to check for other
            // permissions this app might request

    }

    @SuppressWarnings("unchecked")
    private List<String> getNumberUnreadSMS() {
        List<String> lstSms = new ArrayList<String>();
        ContentResolver cr = this.getContentResolver();
        Uri uri = Uri.parse("content://sms/inbox");

        Cursor c = cr.query(uri, // Official CONTENT_URI from docs
                null, // Select body text
                "read = 0",
                null,
                Telephony.Sms.Inbox.DEFAULT_SORT_ORDER); // Default sort order

        int totalSMS = c.getCount();

        if (totalSMS==0){
            lstSms.add("vous n'avez aucun message");
            return lstSms;
        }
        else {
            int j;
            lstSms.add("Vous avez "+totalSMS+" messages");
            if (c.moveToFirst()) {
                for (int i = 0; i < totalSMS; i++) {


                    lstSms.add("vous avez un message du " + c.getColumnIndexOrThrow("adresse")
                    +
                                " qui dit ");
                    lstSms.add(c.getString(c.getColumnIndexOrThrow("body")).toString());

                    c.moveToNext();
                }
            } else {
                throw new RuntimeException("You have no SMS in Inbox");
            }
            c.close();
            return lstSms;
        }
    }
    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "?";

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }

    @Override
	protected void onPause() {
		super.onPause();
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
        mPlayer.release();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
}
