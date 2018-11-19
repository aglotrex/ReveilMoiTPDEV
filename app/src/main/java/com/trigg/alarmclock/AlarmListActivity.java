package com.trigg.alarmclock;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

/**
 * Classe ou sont affichees toutes les alarmes enregistrees en base
 * On peut activer ou desactiver une alarme
 */
public class AlarmListActivity extends ListActivity {

	private AlarmListAdapter mAdapter;

	// Aide a l'utilisation de la base,
    // Ici, on update les alarmes deja existantes
    private AlarmDBHelper dbHelper = new AlarmDBHelper(this);
    private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
	    // Autorise la bar d'action dans le menu
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setTitle("Liste des alarmes");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Set en fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Set le layout
		setContentView(R.layout.activity_alarm_list);

		mAdapter = new AlarmListAdapter(this, dbHelper.getAlarms());
		setListAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Display le menu
		getMenuInflater().inflate(R.menu.alarm_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // Action lors du click sur la bar d'action
		switch (item.getItemId()) {
            // Demande de retour a la page parent qui a appele cet intent
            case android.R.id.home: {
                finish();
                break;
            }
            // Action a effectue lors du click sur ajouter une alarme
			case R.id.action_add_new_alarm: {
				startAlarmDetailsActivity(-1);
				break;
			}
		}

		return super.onOptionsItemSelected(item);
	}

    // Active/Desactive l'alarme et enregistre en base
	public void setAlarmEnabled(long id, boolean isEnabled) {
		AlarmManagerHelper.cancelAlarms(this);
		
		AlarmModel model = dbHelper.getAlarm(id);
		model.isEnabled = isEnabled;
		dbHelper.updateAlarm(model);
		
		AlarmManagerHelper.setAlarms(this);
	}

    // Ouvre l'intent de modification de l'alarme (pareil que celui de creation)
	public void startAlarmDetailsActivity(long id) {
		Intent intent = new Intent(this, AlarmDetailsActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent, 0);
	}

    // Demande la suppression apres un long click
	public void deleteAlarm(long id) {
		final long alarmId = id;
        // Lance une boite de dialogue toute simple
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Voulez vous vraiment supprimer l'alarme ?")
		.setTitle("Supprimer ?")
		.setCancelable(true)
		.setNegativeButton("Abandon", null)
        // Si ok alors on supprime en base avec l'aide de gestion de BD
		.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Cancel Alarms
				AlarmManagerHelper.cancelAlarms(mContext);
				//Delete alarm from DB by id
				dbHelper.deleteAlarm(alarmId);
				//Refresh the list of the alarms in the adaptor
				mAdapter.setAlarms(dbHelper.getAlarms());
				//Notify the adapter the data has changed
				mAdapter.notifyDataSetChanged();
				//Set the alarms
				AlarmManagerHelper.setAlarms(mContext);
			}
		}).show();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            mAdapter.setAlarms(dbHelper.getAlarms());
            mAdapter.notifyDataSetChanged();
        }
    }
}
