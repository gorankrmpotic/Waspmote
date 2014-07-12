package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
		
		List<String> sensorNames = new ArrayList<>();
		List<Integer> lista = sensorMeasurements.getAllSensorIds();
		String senzori = "Senzori su:";
		for (Integer i : lista) {
			sensorNames.add(sensorsDs.getSensorNameById(i));
		}
		Toast.makeText(this, senzori, Toast.LENGTH_LONG).show();
		
		listSensorsWithData.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, sensorNames));
	}

}
