package hr.fer.zari.waspmote.services;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import hr.fer.zari.waspmote.ServiceData;
import hr.fer.zari.waspmote.WaspmoteApplication;
import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import android.R.integer;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
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
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    deviceID = tm.getDeviceId();
		final String add = gsnIp.split(":")[0];
		Log.d("GsnService", gsnIp.split(":")[1]);
		final int port = Integer.parseInt(gsnIp.split(":")[1]);
		Log.d("GSNSevice", "dohvaceni podaci prije slanja");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try
				{
					inetAddress = InetAddress.getByName(add);
					String msg = "15";
					int msgLength = msg.length();
					byte[] message = msg.getBytes();
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
//		try
//		{
//			inetAddress = InetAddress.getByName(add);			
//		}
//		catch(Exception ex)
//		{
//			Log.e("GsnService", ex.getMessage());
//		}
//		String msg = "15";
//		int msgLength = msg.length();
//		byte[] message = msg.getBytes();
//		try
//		{
//			Log.d("GsnService", String.valueOf(inetAddress));
//			DatagramSocket s = new DatagramSocket();
//			DatagramPacket p = new DatagramPacket(message, msgLength, inetAddress, port);
//			Log.d("GsnService", "Data prepared");
//			s.send(p);
//			s.close();
//			Log.d("GsnService", "Data send");
//		}
//		catch(Exception ex)
//		{
//			Log.e("GsnService", ex.getMessage());
//		}
		
		Log.d("GsnService", "Service started");
		
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		
		
		Log.w("GsnService", "Service killed");
	}
	
	public void doWork()
	{
		//dohvati najveci timestamp
		//dohvati sve podatke s tim timestampom
		//parsiraj ih ako je potrebno
		//salji na vec uspostavljenu konekciju
	}
	
	
	
	

}
