package hr.fer.zari.waspmote.services;

import hr.fer.zari.waspmote.ServiceData;
import hr.fer.zari.waspmote.WaspmoteApplication;
import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.models.SensorMeasurement;
import hr.fer.zari.waspmote.models.Sensors;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.util.TimeUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

public class GsnService extends Service {

	ServiceData sd;
	WaspmoteApplication waspApp;
	SensorMeasurementDataSource sensorMeasurementData;
	String gsnIp;
	String gsnUsername;
	String gsnPassword;
	private String deviceID;
	InetAddress inetAddress;
	final Handler handler = new Handler();
	int period;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
//		return super.onStartCommand(intent, flags, startId);
		Log.d("GsnService", "Gsn service start");
		sd = (ServiceData) intent.getExtras().getSerializable("ServiceData");
		waspApp = (WaspmoteApplication)getApplication();
		sensorMeasurementData = (SensorMeasurementDataSource) waspApp.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);
		gsnIp = sd.getGsnIp();
		gsnUsername = sd.getGsnUsername();
		gsnPassword = sd.getGsnPassword();
		period = sd.getPeriod();
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    deviceID = tm.getDeviceId();
		doWork();	
				
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		handler.removeCallbacks(null);
		handler.removeCallbacksAndMessages(null);
		
		
		Log.w("GsnService", "Service killed");
	}
	
	public void doWork()
	{	
		//salji na vec uspostavljenu konekciju
		
		//dohvati najveci timestamp
		Long ts = sensorMeasurementData.getMaxTimestamp();
		if(ts != -1)
		{
			Log.d("GsnService", "Data exists");
			//dohvati sve podatke s tim timestampom
			List<SensorMeasurement> sensMeas = sensorMeasurementData.getAllSensorMeasurementByTimestamp(ts);
			//parsiraj ih ako je potrebno
			//1. interni!!
			String toSendData = "";
			if(sd.containsInternalSensors())
			{
				for(SensorMeasurement sm : sensMeas)
				{
					Sensors s = sd.getSensorById(sm.getIdSensor());
					if(s != null)
					{
						if(s.getSensorType() == 1)
						{
							toSendData += "!"+s.getSensorName() + "!" + sm.getValue() +"!" + s.getSensorName()+"!";							
						}
					}
				}
			}
			if(sd.containsExternalBluetoothSensors())
			{
				for(SensorMeasurement sm : sensMeas)
				{
					Sensors s = sd.getSensorById(sm.getIdSensor());
					if(s != null)
					{
						if(s.getSensorType() == 2)
						{
							String val = sm.getValue();
							//if(!val.startsWith("!"))
							//{
								toSendData += "!"+s.getSensorName()+"!"+val+"!"+s.getSensorName()+"!";
							//} 
						}
					}							
				}
			}
			if (sd.containsExternalUsbSensors()) {
				for(SensorMeasurement sm : sensMeas)
				{
					Sensors s = sd.getSensorById(sm.getIdSensor());
					if(s != null)
					{
						if(s.getSensorType() == 3)
						{
							String val = sm.getValue();		
							toSendData += "!"+s.getSensorName()+"!"+val+"!"+s.getSensorName()+"!";
						}
					}							
				}
			}
			if(!toSendData.endsWith("!end!"))
			{
				toSendData += "!end!";
			}
			final String send = toSendData;
			Log.d("GsnService", send);
			Log.d("GsnService", "Parsing of values finished");
			final String add = gsnIp.split(":")[0];
			Log.d("GsnService", gsnIp.split(":")[1]);
			final int port = Integer.parseInt(gsnIp.split(":")[1]);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try
					{
						inetAddress = InetAddress.getByName(add);						
						int msgLength = send.length();						
						byte[] message = send.getBytes();
						Log.d("GsnService", String.valueOf(inetAddress));
						DatagramSocket s = new DatagramSocket();
						DatagramPacket p = new DatagramPacket(message, msgLength, inetAddress, port);						
						Log.d("GsnService", "Data prepared");
						s.send(p);
						s.close();
						Log.d("GsnService", "Data send");
					}
					catch(Exception ex)
					{
						Log.e("GsnService", ex.getMessage());
					}			
				}
			}).start();	
		}
		handler.postDelayed(new Runnable()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				doWork();
			}
			
			// TODO vrati na staro  TimeUnit.MINUTES.toMillis(period));
		}, TimeUnit.SECONDS.toMillis(10));
	}
	
	
	
	

}
