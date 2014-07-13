package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.models.Sensors;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Ova klasa izlistava sve senzore za koje postoje spremljeni podaci u bazi
 * podataka. Na klik nekog od senzora zove novu aktivnost koja mu daje na
 * mogu'nost odabira kojeg podatka s kojeg senzora.
 * 
 */
public class ListSensorsDatabaseDataActivity extends Activity {

	private ListView listSensorsWithData;

	private WaspmoteApplication waspApp;
	private SensorsDataSource sensorsDs;
	private SensorMeasurementDataSource sensorMeasurements;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_sensors_database_data);

		listSensorsWithData = (ListView) findViewById(R.id.sensors_with_data_list);

		waspApp = (WaspmoteApplication) getApplication();
		sensorsDs = (SensorsDataSource) waspApp.getWaspmoteSqlHelper().getSensorsDataSource(this);
		sensorMeasurements = (SensorMeasurementDataSource) waspApp
				.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);
		
		List<Sensors> sensors = new ArrayList<>();
		List<String> sensorNames = new ArrayList<>();
		List<Integer> lista = sensorMeasurements.getAllSensorIds();
		for (Integer i : lista) {
			sensors.add(sensorsDs.getSensorById(i));
			sensorNames.add(sensorsDs.getSensorNameById(i));
		}
		final List<Sensors> sensorsToListener = sensors;
		listSensorsWithData.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, sensorNames));
		
		listSensorsWithData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
			public void onItemClick(AdapterView<?> av, View view, int i, long l) {
	            Intent intent;
	            if (1 == sensorsToListener.get(i).getSensorType()) {
	            	intent = new Intent(ListSensorsDatabaseDataActivity.this, PlotActivity.class);
	            	intent.putExtra("sensorId", sensorsToListener.get(i).get_id());
	            } else {
	            	intent = new Intent(ListSensorsDatabaseDataActivity.this, ListSensorsDataTypeActivity.class);
	            	intent.putExtra("sensorId", sensorsToListener.get(i).get_id());
	            }
	            startActivity(intent);
	        }
	    });
	}

}
