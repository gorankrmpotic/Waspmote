package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.SensorTypeDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.models.Sensors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class ExternalSensorActivity extends ActionBarActivity {

	BluetoothAdapter bluetooth;
	AlertDialog adDevices;
	BluetoothSocket connectionSocket = null;
	BluetoothDevice sensorDevice = null;
//	private static final UUID MY_UUID = UUID.fromString("0000110a-0000-1000-8000-00805F9B34FB");
	OutputStream outStream = null;
	InputStream inStream = null;
	boolean connected = false;
	WaspmoteApplication waspApp;
	SensorsDataSource sensorData;
	SensorTypeDataSource sensorTypeData;
	private CountDownTimer timer;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_external_sensor);
		setTitle("External Sensor");
		bluetooth = BluetoothAdapter.getDefaultAdapter();
		ChangeCloseConnectionButtonVisibility();
		ChangeGetButtonVisibility();
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		CheckAndChangeButtonVisibility();
		waspApp = (WaspmoteApplication)getApplication();
		sensorData = (SensorsDataSource) waspApp.getWaspmoteSqlHelper().getSensorsDataSource(this);
		sensorTypeData = (SensorTypeDataSource) waspApp.getWaspmoteSqlHelper().getSensorTypeDataSource(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.external_sensor, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_external_sensor,
					container, false);
			return rootView;
		}
	}
	
	public void FindSensorButtonClicked(View view)
	{
		//paljenje bluetootha i sve potrebno uz to....da li je potrebno raditi novi activity???
		if(!bluetooth.isEnabled())
		{
			bluetooth.enable();
			ButtonVisible();
		}
		//traži uređaje koji su u blizini i omogući korisniku spajanje :D
		SearchForDevices();
	}
	
	public void SearchForDevices()
	{
		AlertDialog.Builder tmpBuilder = new AlertDialog.Builder(ExternalSensorActivity.this);
		final AlertDialog.Builder adb = new AlertDialog.Builder(ExternalSensorActivity.this);
		adb.setTitle("Devices:");
		adb.setNegativeButton("Cancel", null);
		
		final AlertDialog tmpAdb = tmpBuilder.create();
		tmpAdb.setTitle("Please wait");
		tmpAdb.setMessage("Scaning for devices...");
		tmpAdb.show();
		bluetooth.startDiscovery();
		
		final ArrayAdapter<HashMap<String, String>> mAdapter = new ArrayAdapter<HashMap<String, String>>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		
		final ArrayList<HashMap<String, String>> arl = new ArrayList<HashMap<String, String>>();
		BroadcastReceiver recv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action))
				{
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					HashMap<String, String> hm = new HashMap<String, String>();
					hm.put(device.getName(), device.getAddress());
					arl.add(hm);
					mAdapter.add(hm);
				}
				if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
				{
					tmpAdb.cancel();
					adDevices.show();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(recv, filter);
			
		adb.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Object[] deviceName = mAdapter.getItem(which).keySet().toArray();
				String deviceNameString = deviceName[0].toString();
				Object[] deviceMac = mAdapter.getItem(which).values().toArray();
				String deviceMacString = deviceMac[0].toString();
				Boolean same = false;
				//dohvatio sam uredaj!!
				
				if(sensorDevice != null)
				{
					if(sensorDevice.getAddress().trim().equals(deviceMacString.trim()))
					{
						if(inStream != null || outStream != null || connectionSocket != null)
						{
							AlertDialog.Builder alreadyConnected = new AlertDialog.Builder(ExternalSensorActivity.this);
							alreadyConnected.setTitle("Already connected");
							alreadyConnected.setMessage("You are already connected to this sensor!");
							alreadyConnected.setPositiveButton("Ok", null);
							alreadyConnected.show();
							return;
						}
					}
					else
					{
						CloseConnection();
					}
				}
				
				sensorDevice = bluetooth.getRemoteDevice(deviceMacString);
				
				try
				{
		            
		            MakeConnectionAndStreams();
		            connected = true;
		            
		            ChangeCloseConnectionButtonVisibility();
					ChangeGetButtonVisibility();
					
				}
				catch(Exception ex)
				{
					AlertDialog.Builder pao = new AlertDialog.Builder(ExternalSensorActivity.this);
					pao.setTitle("Error");
					pao.setMessage(ex.getMessage());
					pao.setPositiveButton("Ok", null);
					pao.show();
				}
				
				
			}
		});
		
		adb.setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//ovaj clear je najvjerojatnije nepotreban ali neka se na�e
				mAdapter.clear();
				SearchForDevices();
			}
		});
		adDevices = adb.create();
		
	}
	
	public void MakeConnectionAndStreams()
	{
		try
		{
			if(connectionSocket != null)
			{
				CloseConnection();
			}
			UUID uuid = sensorDevice.getUuids()[0].getUuid();
			connectionSocket = sensorDevice.createInsecureRfcommSocketToServiceRecord(uuid);
	        bluetooth.cancelDiscovery();
	        try
	        {
	        	connectionSocket.connect();
	        	inStream = connectionSocket.getInputStream();
	        	outStream = connectionSocket.getOutputStream();
	        	int bluetoothSensorTypeId = sensorTypeData.getSensorTypeByName("External Bluetooth").get_id();
	        	sensorData.addSensor(sensorDevice.getName(), bluetoothSensorTypeId);
	        }
	        catch(Exception ex)
	        {
	        	connectionSocket.close();
	        	inStream.close();
	        	outStream.close();
	        	AlertDialog.Builder errorConnect = new AlertDialog.Builder(ExternalSensorActivity.this);
	        	errorConnect.setTitle("Error!");
	        	errorConnect.setMessage(ex.getMessage());
	        	errorConnect.setPositiveButton("Ok",null);
	        	errorConnect.show();
	        }
		}
		catch(Exception ex)
		{
			AlertDialog.Builder errorReset = new AlertDialog.Builder(ExternalSensorActivity.this);
			errorReset.setTitle("Error!");
			errorReset.setMessage(ex.getMessage());
			errorReset.setPositiveButton("Ok", null);
			errorReset.show();
		}
	}
	
	
	public void TurnOffBTButtonClicked(View view)
	{
		if(bluetooth.isEnabled())
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(ExternalSensorActivity.this);
			adb.setTitle("Warning!");
			adb.setMessage("Turning off Bluetooth adapter will disable receiving data from external sensor!");
			//buttoni su naopako zbog defaultne njihove pozicije!!
			adb.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					bluetooth.disable();
					ButtonGone();
				}
			});
			adb.setPositiveButton("Cancel", null);
			adb.show();
		}
	}
	
	public void CheckAndChangeButtonVisibility()
	{
		if(bluetooth.isEnabled())
		{
			Button turnOffButton = (Button)findViewById(R.id.TurnOffBTButton);
			turnOffButton.setVisibility(View.VISIBLE);
		}
		else if(!bluetooth.isEnabled())
		{
			Button turnOffButton = (Button)findViewById(R.id.TurnOffBTButton);
			turnOffButton.setVisibility(View.GONE);
		}
	}
	
	//ove dvije metode su potrebne zbog delaya prilikom paljenja bluetootha!!
	//zbog tog delaya medota CheckAndChangeButtonVisibility krivo detektira stanje bluetootha
	public void ButtonVisible()
	{
		Button turnOffButton = (Button)findViewById(R.id.TurnOffBTButton);
		turnOffButton.setVisibility(View.VISIBLE);
	}
	
	public void ButtonGone()
	{
		Button turnOffButton = (Button)findViewById(R.id.TurnOffBTButton);
		turnOffButton.setVisibility(View.GONE);
		connected = false;
		ChangeCloseConnectionButtonVisibility();
		ChangeGetButtonVisibility();
	}
	
	public void ChangeCloseConnectionButtonVisibility()
	{
		Button closeConnectioButton = (Button)findViewById(R.id.CloseConnectionButton);
		if(connected == true)
		{
			closeConnectioButton.setVisibility(View.VISIBLE);
		}
		else
		{
			closeConnectioButton.setVisibility(View.GONE);
		}
	}
	
	public void CloseConnectionButtonClicked(View view)
	{
		CloseConnection();
	}
	
	public void CloseConnection()
	{
		int cnt = 0;
		if(inStream != null)
		{
			try
			{
				inStream.close();
				cnt++;
			}
			catch(Exception ex)
			{
				
			}
			inStream = null;
		}
		if(outStream != null)
		{
			try
			{
				outStream.close();
				cnt++;
			}
			catch(Exception ex)
			{
				
			}
			outStream = null;
		}
		if(connectionSocket != null)
		{
			try
			{
				connectionSocket.close();
				cnt++;
			}
			catch(Exception ex)
			{
				
			}
			connectionSocket = null;
		}
		if(cnt == 3)
		{
			connected = false;
		}
		ChangeCloseConnectionButtonVisibility();
		ChangeGetButtonVisibility();
	}
	
	public void ChangeGetButtonVisibility()
	{
		Button getDataButton = (Button) findViewById(R.id.GetExternalDataButton);
		if(connected == true)
		{
			getDataButton.setVisibility(View.VISIBLE);
		}
		else
		{
			getDataButton.setVisibility(View.GONE);
		}
	}
	
	public void GetDataButtonClicked(View view)
	{
		final AlertDialog.Builder getDataDialog = new AlertDialog.Builder(ExternalSensorActivity.this);

		getDataDialog.setPositiveButton("Ok", null);
//		byte[] buffer = new byte[1024];
//		buffer = "Data".getBytes();
//		try
//		{
//			outStream.write(buffer);
//			outStream.flush();
//		}
//		catch(Exception ex)
//		{
//			AlertDialog.Builder exc = new AlertDialog.Builder(ExternalSensorActivity.this);
//			exc.setTitle("Error while requesting");
//			exc.setMessage(ex.getMessage());
//			exc.setPositiveButton("Ok", null);
//			exc.show();
//		}
//		buffer = new byte[1024];
		byte[] readBuff = new byte[1024];			
		try
		{			
			int timeout = 0; 
			int maxTimeout = 40;
//			inStream.skip(inStream.available());
			while(inStream.available() != 0)
			{
				inStream.skip(inStream.available());
			}
			while(inStream.available() == 0 && timeout < maxTimeout)
			{
				timeout++;
				Thread.sleep(50);
			}
			inStream.read(readBuff);
			String dataReceived = new String(readBuff);
			getDataDialog.setTitle("Data");
			getDataDialog.setMessage(dataReceived);
			getDataDialog.show();
			
		}
		catch(Exception ex)
		{			
			AlertDialog.Builder exc = new AlertDialog.Builder(ExternalSensorActivity.this);
			exc.setTitle("Error");
			exc.setMessage("Connection Timed out");
			exc.setPositiveButton("Ok", null);
			exc.show();
		}	

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		CloseConnection();
		super.onBackPressed();
	}
	
	
	
	
}
