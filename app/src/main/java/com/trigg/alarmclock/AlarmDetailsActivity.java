package com.trigg.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmDetailsActivity extends Activity {

    // Aide a l'utilisation de la base
    // Ici il va permettre d'appeler la methode de creation en base
	private AlarmDBHelper dbHelper = new AlarmDBHelper(this);

    // Conteneur de l'alarme a mettre en base
	private AlarmModel alarmDetails;

    // Valeurs de l'alarme
	private TimePicker timePicker;
	private EditText edtName;
	private CustomSwitch chkWeekly;
	private CustomSwitch chkSunday;
	private CustomSwitch chkMonday;
	private CustomSwitch chkTuesday;
	private CustomSwitch chkWednesday;
	private CustomSwitch chkThursday;
	private CustomSwitch chkFriday;
	private CustomSwitch chkSaturday;
	private TextView txtToneSelection;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Autorise la bar d'action a etre display
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
        // Set en fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Set le layout
		setContentView(R.layout.activity_details);

        // Set le title dans la bar d'action
		getActionBar().setTitle("Creer une alarme");
		getActionBar().setDisplayHomeAsUpEnabled(true);

        // Recuperation des references de tous les elements du layout necessaires
        // a la mise en place de l'alarme en base
		timePicker = (TimePicker) findViewById(R.id.alarm_details_time_picker);
		edtName = (EditText) findViewById(R.id.alarm_details_name);
		chkWeekly = (CustomSwitch) findViewById(R.id.alarm_details_repeat_weekly);
		chkSunday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_sunday);
		chkMonday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_monday);
		chkTuesday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_tuesday);
		chkWednesday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_wednesday);
		chkThursday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_thursday);
		chkFriday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_friday);
		chkSaturday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_saturday);
		txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
		
		long id = getIntent().getExtras().getLong("id");
		
		if (id == -1) {
			alarmDetails = new AlarmModel();
		} else {
            // Mise en place des valeurs pour l'enregistrement futur
			alarmDetails = dbHelper.getAlarm(id);
			
			timePicker.setCurrentMinute(alarmDetails.timeMinute);
			timePicker.setCurrentHour(alarmDetails.timeHour);

            // Recuperation du nom de l'alarme
			edtName.setText(alarmDetails.name);

            // Check quels jours sont toggle on ou off
			chkWeekly.setChecked(alarmDetails.repeatWeekly);
			chkSunday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.SUNDAY));
			chkMonday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.MONDAY));
			chkTuesday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.TUESDAY));
			chkWednesday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.WEDNESDAY));
			chkThursday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.THURSDAY));
			chkFriday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.FRDIAY));
			chkSaturday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.SATURDAY));

            // Recuperation de l'alarme
			txtToneSelection.setText(RingtoneManager.getRingtone(this, alarmDetails.alarmTone).getTitle(this));
		}

		final LinearLayout ringToneContainer = (LinearLayout) findViewById(R.id.alarm_ringtone_container);
		ringToneContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				startActivityForResult(intent , 1);
			}
		});
	}

    // Lors du click pour changer la sonnerie, il faut display les sonneries disponibles
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
	        switch (requestCode) {
		        case 1: {
		        	alarmDetails.alarmTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
		        	txtToneSelection.setText(RingtoneManager.getRingtone(this, alarmDetails.alarmTone).getTitle(this));
		            break;
		        }
		        default: {
		            break;
		        }
	        }
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // Ajout du menu dans la bar d'action
		getMenuInflater().inflate(R.menu.alarm_details, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

        // Lors d'une action sur la bar d'action
		switch (item.getItemId()) {
            // Demande de retour a la page parent qui a appele cet intent
			case android.R.id.home: {
				finish();
				break;
			}
            // Enregistrement en base l'alarme nouvellement creee
			case R.id.action_save_alarm_details: {
				updateModelFromLayout();

                // Si elle existe deja on annule cette alarme
				AlarmManagerHelper.cancelAlarms(this);
				// On enregistre les alarmes uniquement si l'id est -1
                // Sinon on modifie
				if (alarmDetails.id < 0) {
					dbHelper.createAlarm(alarmDetails);
				} else {
					dbHelper.updateAlarm(alarmDetails);
				}

                // Mise en place de l'alarme
				AlarmManagerHelper.setAlarms(this);

                setResult(RESULT_OK);
				finish();
			}
		}

		return super.onOptionsItemSelected(item);
	}

    // Mise a jour du layout apres action sur le menu
	private void updateModelFromLayout() {

		alarmDetails.timeMinute = timePicker.getCurrentMinute().intValue();
		alarmDetails.timeHour = timePicker.getCurrentHour().intValue();
		alarmDetails.name = edtName.getText().toString();
		alarmDetails.repeatWeekly = chkWeekly.isChecked();	
		alarmDetails.setRepeatingDay(AlarmModel.SUNDAY, chkSunday.isChecked());	
		alarmDetails.setRepeatingDay(AlarmModel.MONDAY, chkMonday.isChecked());	
		alarmDetails.setRepeatingDay(AlarmModel.TUESDAY, chkTuesday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.WEDNESDAY, chkWednesday.isChecked());	
		alarmDetails.setRepeatingDay(AlarmModel.THURSDAY, chkThursday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.FRDIAY, chkFriday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.SATURDAY, chkSaturday.isChecked());
		alarmDetails.isEnabled = true;

	}
	
}
