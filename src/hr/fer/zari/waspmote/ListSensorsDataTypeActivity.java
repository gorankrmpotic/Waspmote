package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.models.SensorMeasurement;

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
import android.widget.Toast;

/**
 * Parsira podatke spremljene u bazi i daje korisniku na izbor koji tip podatka zeli prikazati.
 * <p>
 * Eksterni senzor je dao string tipa !b!45!b!!ax!100!ax! , tada 'e ova aktivnost korisniku dati dva izbora:
 * b  i  ax.
 * </p>
 *
 */
public class ListSensorsDataTypeActivity extends Activity {

	private ListView sensorDataTypes;

	private WaspmoteApplication waspApp;
	private SensorsDataSource sensorsDs;
	private SensorMeasurementDataSource sensorMeasurements;
	
	private int sensorId;
	private String dataType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_sensors_data_type);

		sensorDataTypes = (ListView) findViewById(R.id.sensor_data_types);
		
		waspApp = (WaspmoteApplication) getApplication();
		sensorsDs = (SensorsDataSource) waspApp.getWaspmoteSqlHelper().getSensorsDataSource(this);
		sensorMeasurements = (SensorMeasurementDataSource) waspApp
				.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			//Toast.makeText(this, "Unable to comply, exiting.", Toast.LENGTH_SHORT).show();
		} else {
			sensorId = extras.getInt("sensorId", 0);
			//Toast.makeText(this, "sensorid = " + sensorId, Toast.LENGTH_SHORT).show();
		}
		
		List<String> dataTypes = fillDataTypes();
		
		sensorDataTypes.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, dataTypes));
		
		sensorDataTypes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
			public void onItemClick(AdapterView<?> av, View view, int i, long l) {
	            Intent intent;
	            intent = new Intent(ListSensorsDataTypeActivity.this, PlotActivity.class);
	            	intent.putExtra("sensorId", sensorId);
	            	intent.putExtra("dataType", (String) sensorDataTypes.getItemAtPosition(i));
	            startActivity(intent);
	        }
	    });
	}

	private List<String> fillDataTypes() {
		List<SensorMeasurement> measurements = sensorMeasurements.getAllSensorMeasurementBySensorId(sensorId);
		String measurement = measurements.get(0).getValue();
		List<String> dataTypes = new ArrayList<>();
		String type;
		while(measurement.length() > 0) {
			Log.d("hahaha",  measurement);

			measurement = measurement.substring(1);
			type = measurement.substring(0, measurement.indexOf("!"));
			dataTypes.add(type);
			if (measurement.split("!" + type + "!").length == 1) {
				measurement = "";
			} else {
				measurement = measurement.split("!" + type + "!")[1];				
			}
		}
		return dataTypes;
	}
}
