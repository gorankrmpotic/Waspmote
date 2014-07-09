package hr.fer.zari.waspmote.services;

import hr.fer.zari.waspmote.ServiceData;
import hr.fer.zari.waspmote.WaspmoteApplication;
import hr.fer.zari.waspmote.mSensor;
import hr.fer.zari.waspmote.mSensors;
import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.models.SensorMeasurement;
import hr.fer.zari.waspmote.models.Sensors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SensorMeasurementService extends Service implements SensorEventListener {

	int period;	
	mSensors internalSensors;
	BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();		
	String sensAddress ="";
	BluetoothDevice sensorDevice = null;
	BluetoothSocket connectionSocket = null;
	OutputStream outStream = null;
	InputStream inStream = null;	
	List<SensorMeasurement> sensorMeasurementsToWrite = new ArrayList<SensorMeasurement>();
	static SensorManager mgr;
	ServiceData sd;
	final Handler handler = new Handler();
	SensorMeasurementDataSource sensorMeasurementData;
	WaspmoteApplication waspApp;
	boolean first = true;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//return super.onStartCommand(intent, flags, startId);
		sd = (ServiceData) intent.getExtras().getSerializable("ServiceData");
		waspApp = (WaspmoteApplication)getApplication();
		sensorMeasurementData = (SensorMeasurementDataSource) waspApp.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);		
		period = sd.getPeriod();
		Log.d("SensorMeasurementService", "Service started");
		if(sd.containsExternalBluetoothSensors())
		{			
			final Sensors extSensor = sd.getExternalBluetoothSensors().get(0);			
			if(!bluetooth.isEnabled())
			{
				bluetooth.enable();
			}
			bluetooth.startDiscovery();
			BroadcastReceiver recv = new BroadcastReceiver()
			{

				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					String action = intent.getAction();
					if(BluetoothDevice.ACTION_FOUND.equals(action))
					{
						BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						if(device.getName().equals(extSensor.getSensorName()))
						{
							sensAddress = device.getAddress();
						}
					}
					if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
					{
						MakeConnection();
						doWork();
					}
				}
				
			};
			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(recv, filter);	
			
		}
		if(sd.containsInternalSensors())
		{
			mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		  	List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);
		  	List<Sensors> intSens = sd.getInternalSensors();
		  	List<String> intSensNames = new ArrayList<String>();
		  	List<Sensor> selectedSensors = new ArrayList<Sensor>();
		  	for(Sensors sen : intSens)
		  	{
		  		intSensNames.add(sen.getSensorName());
		  	}
		  	for(Sensor sen : sensors)
		  	{
		  		if(intSensNames.contains(sen.getName()))
		  		{
		  			selectedSensors.add(sen);
		  		}
		  	}
		  	internalSensors = new mSensors(selectedSensors, mgr);
		  	for(mSensor sen : internalSensors.getAllSensors())
		  	{
		  		mgr.registerListener(this, sen.getSensor(), SensorManager.SENSOR_DELAY_NORMAL);
		  	}
		}
		
		if(!sd.containsExternalBluetoothSensors())
		{
			doWork();
		}

	    return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		handler.removeCallbacks(null);
		handler.removeCallbacksAndMessages(null);
		if(inStream != null)
		{
			try
			{
				inStream.close();
			}
			catch(Exception ex)
			{
				
			}
		}
		if(outStream != null)
		{
			try
			{
				outStream.close();
			}
			catch(Exception ex)
			{
				
			}
		}
		if(connectionSocket != null)
		{
			try
			{
				connectionSocket.close();
			}
			catch(Exception ex)
			{
				
			}
			connectionSocket = null;
		}
		Log.w("SensorMeasurementService", "Service killed");
	}
	
	



	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		mSensor changedValueSensor = internalSensors.getSensorByName(event.sensor.getName());
		if(changedValueSensor != null)
		{
			changedValueSensor.setSensorValue(event.values[0]);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	public void doWork()
	{		
		Long ts = System.currentTimeMillis();
		if(sd.containsInternalSensors())
		{
			if(!first)
			{				
				for(mSensor sen : internalSensors.getAllSensors())
				{					
					int id = sd.getSensorIdByName(sen.getSensorName());
					if(id != -1)
					{					
						sensorMeasurementData.addSensorMeasurement(id, ts, String.valueOf(sen.getSensorValue()), "N/A");
						Log.d("SensorMeasurementService to database", "id: "+String.valueOf(id)+" ts: "+String.valueOf(ts)+" value: "+String.valueOf(sen.getSensorValue()));
					}
				}
			}
			first = false;
		}
		if(sd.containsExternalBluetoothSensors())
		{
//			byte[] buffer = new byte[1024];
//			buffer = "Data".getBytes();
//			try
//			{
//				outStream.write(buffer);
//				outStream.flush();
//			}
//			catch(Exception ex)
//			{
//				Log.e("SensorMeasurementService", ex.getMessage());
//			}
			byte[] readBuff = new byte[1024];
			try
			{
//				int timeout = 0; 
//				int maxTimeout = 8;
//				while(inStream.available() == 0 && timeout < maxTimeout)
//				{
//					timeout++;
//					Thread.sleep(250);
//				}
				int timeout = 0; 
				int maxTimeout = 40;
				//inStream.skip(inStream.available());
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
				String dataReceived = new String(readBuff).trim();			
				int id = sd.getSensorIdByName(sd.getExternalBluetoothSensors().get(0).getSensorName());
				if(id != -1)
				{
					sensorMeasurementData.addSensorMeasurement(id, ts, dataReceived, "N/A");
					Log.d("SensorMeasurementService to database BT", "id: "+String.valueOf(id)+" ts: "+String.valueOf(ts)+" value: "+dataReceived);
				}
			}
			catch(Exception ex)
			{
				Log.e("SensorMeasurementService", ex.getMessage());
			}
		}
//		final Handler handler = new Handler();
		
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub				
				doWork();
			}
		}, TimeUnit.MINUTES.toMillis(period));			
	}
	
	public void MakeConnection()
	{
		if(sensAddress.isEmpty())
		{			
		}
		else
		{	
			sensorDevice = bluetooth.getRemoteDevice(sensAddress);
			UUID uuid = sensorDevice.getUuids()[0].getUuid();
			try
			{
				connectionSocket = sensorDevice.createInsecureRfcommSocketToServiceRecord(uuid);				
				bluetooth.cancelDiscovery();
				connectionSocket.connect();
		        inStream = connectionSocket.getInputStream();
	        	outStream = connectionSocket.getOutputStream();		        	
			}
			catch(Exception ex)
			{
				Log.e("SensorMeasurementService", ex.getMessage());
			}	         
		}
	}
	
	
	
	

}
