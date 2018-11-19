package com.trigg.alarmclock;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmListAdapter extends BaseAdapter {

	private Context mContext;
	private List<AlarmModel> mAlarms;

	public AlarmListAdapter(Context context, List<AlarmModel> alarms) {
		mContext = context;
		mAlarms = alarms;
	}

    // Set la liste de toutes les alarmes
	public void setAlarms(List<AlarmModel> alarms) {
		mAlarms = alarms;
	}

    // Compte toutes le nombre d'alarme enregistree en base
	@Override
	public int getCount() {
		if (mAlarms != null) {
			return mAlarms.size();
		}
		return 0;
	}

    // Recupere une alarme en fonction de sa position dans la liste
	@Override
	public Object getItem(int position) {
		if (mAlarms != null) {
			return mAlarms.get(position);
		}
		return null;
	}

    // Recupere l'id d'une alarme
	@Override
	public long getItemId(int position) {
		if (mAlarms != null) {
			return mAlarms.get(position).id;
		}
		return 0;
	}

    // Creation de la liste des des alarmes
	@Override
	public View getView(int position, View view, ViewGroup parent) {
        // Check si une vue est deja existante sinon creation d'une nouvelle
        // hint: c'est pour de meilleures performances
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.alarm_list_item, parent, false);
		}

        // Recuperation d'une reference sur l'alarme enregistree en base
		AlarmModel model = (AlarmModel) getItem(position);

        // Creation des textes et jours coches
        // Creation de l'heure a laquelle sonne le reveil
		TextView txtTime = (TextView) view.findViewById(R.id.alarm_item_time);
		txtTime.setText(String.format("%02d : %02d", model.timeHour, model.timeMinute));
		// Creation du nom de l'alarme pour la liste
		TextView txtName = (TextView) view.findViewById(R.id.alarm_item_name);
		txtName.setText(model.name);

        // Si un jour est coche on change sa couleur
        // ex: si jeudi est a true on le met en vert
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_sunday), model.getRepeatingDay(AlarmModel.SUNDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_monday), model.getRepeatingDay(AlarmModel.MONDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_tuesday), model.getRepeatingDay(AlarmModel.TUESDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_wednesday), model.getRepeatingDay(AlarmModel.WEDNESDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_thursday), model.getRepeatingDay(AlarmModel.THURSDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_friday), model.getRepeatingDay(AlarmModel.FRDIAY));		
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_saturday), model.getRepeatingDay(AlarmModel.SATURDAY));

        // Creation du bouton on/off de l'alarme
		ToggleButton btnToggle = (ToggleButton) view.findViewById(R.id.alarm_item_toggle);
		btnToggle.setChecked(model.isEnabled);
		btnToggle.setTag(Long.valueOf(model.id));
		btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			// si le bouton est a ON, changement en base
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				((AlarmListActivity) mContext).setAlarmEnabled(((Long) buttonView.getTag()).longValue(), isChecked);
			}
		});

        // Creation du listener sur le click d'une des alarmes pour appeler la page de modification de cette derniere
		view.setTag(Long.valueOf(model.id));
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				((AlarmListActivity) mContext).startAlarmDetailsActivity(((Long) view.getTag()).longValue());
			}
		});

		// Creation du listener sur le click d'une alarme pour appeler la boite de dialogue pour supprimer l'alarme
		view.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View view) {
				((AlarmListActivity) mContext).deleteAlarm(((Long) view.getTag()).longValue());
				return true;
			}
		});
		
		return view;
	}

    // Methode de mise a jour de la couleur des jours d'une alarme
	private void updateTextColor(TextView view, boolean isOn) {
		if (isOn) {
			view.setTextColor(Color.GREEN);
		} else {
			view.setTextColor(Color.BLACK);
		}
	}

}
