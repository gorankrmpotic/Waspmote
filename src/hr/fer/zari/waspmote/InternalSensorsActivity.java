package hr.fer.zari.waspmote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class InternalSensorsActivity extends ActionBarActivity implements SensorEventListener {

	private ListView lw;
	mSensors intSensors;
	SensorManager mgr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_internal_sensors);
		setTitle("Internal SensorsTable");
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}		

		GetAllSensors();
		FillListViewSensors();
		  
	}
	
	public void GetAllSensors()
  {		
  	mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);		
  	List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);   
  	intSensors = new mSensors(sensors, mgr);  
  	RegisterListeners();    	  
  }
	
	public void RegisterListeners()
	{
		for(mSensor mSens : intSensors.getAllSensors())
		{
			mgr.registerListener(this, mSens.getSensor(), SensorManager.SENSOR_DELAY_NORMAL);			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.internal_sensors, menu);
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
			View rootView = inflater.inflate(
					R.layout.fragment_internal_sensors, container, false);
			return rootView;
		}
	}
	
	public void FillListViewSensors()
	{
		lw = (ListView) findViewById(R.id.SensorsListView);
		List<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
		for(mSensor mSens : intSensors.getAllSensors())
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("SensorName", mSens.getSensorName());
			maps.add(map);
		}
		String[] from = new String[]{"SensorName"};
		int[] to = {R.id.SensorNameTextView};
		SimpleAdapter mAdapter = new SimpleAdapter(this, maps, R.layout.activity_internal_sensors, from, to);
		lw.setAdapter(mAdapter);
		lw.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
			{
				String ItemValue = lw.getItemAtPosition(position).toString();
				String[] value = ItemValue.split("=");
				String SensorName = "";
				if(value[1].charAt(value[1].length()-1) == '}')
				{
					SensorName = value[1].substring(0, value[1].length()-1);
				}
				mSensor selectedSensor = intSensors.getSensorByName(SensorName);
				AlertDialog.Builder adb = new AlertDialog.Builder(InternalSensorsActivity.this);
				adb.setTitle(selectedSensor.getSensorName()+":");				
				adb.setMessage(Float.toString(selectedSensor.getSensorValue()));				
				adb.setPositiveButton("Ok", null);
				adb.show();    
			}		
		});
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		mSensor changedValueSens = intSensors.getSensorByName(event.sensor.getName());
		changedValueSens.setSensorValue(event.values[0]);		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
