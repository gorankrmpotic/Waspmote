package hr.fer.zari.waspmote;



import hr.fer.zari.waspmote.db.dao.SensorTypeDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.db.dao.UserDataSource;
import hr.fer.zari.waspmote.models.SensorType;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class StartActivity extends ActionBarActivity {
	
	//private String deviceID;
	WaspmoteApplication waspApp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
//		TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//	    deviceID = tm.getDeviceId();
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}	
		waspApp =(WaspmoteApplication) getApplicationContext();
		try
		{
		UserDataSource userData =(UserDataSource) waspApp.getWaspmoteSqlHelper().getUserDataSource(this);
		SensorTypeDataSource sensorTypeData = (SensorTypeDataSource) waspApp.getWaspmoteSqlHelper().getSensorTypeDataSource(this);
		SensorsDataSource sensorsData = (SensorsDataSource) waspApp.getWaspmoteSqlHelper().getSensorsDataSource(this);
		//ovo je potrebno ako korisnik obri�e sve accounte ne�e mo� koristiti nijedan admin account
		
		userData.addUser("admin", "administrator", "admin", "admin");
		sensorTypeData.addSensorType("Internal");
		sensorTypeData.addSensorType("External Bluetooth");
		sensorTypeData.addSensorType("External Cable");
		SensorType sType = sensorTypeData.getSensorTypeByName("Internal");
		//punjenje baze internim senzorima!!
		SensorManager mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> internalSens = mgr.getSensorList(Sensor.TYPE_ALL);
		for(Sensor s : internalSens)
		{
			sensorsData.addSensor(s.getName(), sType.get_id());		
		}
		sensorsData.addSensor("GPS", sType.get_id());
		}
		catch(Exception ex)
		{
			AlertDialog.Builder aa = new AlertDialog.Builder(StartActivity.this);
			aa.setTitle("Exception");
			aa.setMessage(ex.getLocalizedMessage());
			aa.setPositiveButton("OK", null);
			aa.show();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_start,
					container, false);
			return rootView;
		}
	}
		
	public void LogInAsGuestButtonClicked(View view)
	{
		waspApp.setCurrentUser(null);
		Intent MainIntent = new Intent(this, MainActivity.class);
		startActivity(MainIntent);
	}
	
	public void LogInButtonClicked(View view)
	{
		Intent LogInIntent = new Intent(this, LogInActivity.class);		
		startActivity(LogInIntent);
	}
	
	public void ExitButtonClicked(View view)
	{
		/*
		 *  u androidu se nikad program ne prekida s naredbom System.exit(0) jer izaziva
		 *   nepredvidjeno ponasanje
		 */
		this.finish();
	}

}
