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
import android.content.ReceiverCallNotAllowedException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;

public class SensorMeasurementService extends Service implements
		SensorEventListener {

	int period;
	mSensors internalSensors;
	BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
	String sensAddress = "";
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
	Long ts;

	/* usb sensors variables */
	private Context usbDeviceContext;
	private D2xxManager ftdid2xx;
	private FT_Device ftDev = null;
	private int DevCount = 0;
	private int ftDevId;
	/* read usb data variables */
	private readThread read_thread;
	private static final int readLength = 512;
	private int iavailable;
	byte[] readData;
	char[] readDataToText;
	boolean bReadThreadGoing;
	boolean uart_configured;
	boolean connected;
	String data;
	int times = 0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// return super.onStartCommand(intent, flags, startId);
		sd = (ServiceData) intent.getExtras().getSerializable("ServiceData");
		waspApp = (WaspmoteApplication) getApplication();
		sensorMeasurementData = (SensorMeasurementDataSource) waspApp
				.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);
		period = sd.getPeriod();

		readData = new byte[readLength];
		readDataToText = new char[readLength];

		Log.d("SensorMeasurementService", "Service started");
		Toast.makeText(this, "Service started!", Toast.LENGTH_SHORT).show();

		/*
		 * bluetooth senzori sustavljeni prvi jer uspostava konekcije putem
		 * bluetooth a najdulje traje, a dok se uspostavlja eventualna konekcija
		 * putem bluetootha u meduvremenu se mogu uspostaviti veze prema drugim
		 * senzorima
		 */
		if (sd.containsExternalBluetoothSensors()) {
			final Sensors extSensor = sd.getExternalBluetoothSensors().get(0);
			if (!bluetooth.isEnabled()) {
				bluetooth.enable();
			}
			bluetooth.startDiscovery();
			BroadcastReceiver recv = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (BluetoothDevice.ACTION_FOUND.equals(action)) {
						BluetoothDevice device = intent
								.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						if (device.getName().equals(extSensor.getSensorName())) {
							sensAddress = device.getAddress();
						}
					}
					if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
							.equals(action)) {
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

		if (sd.containsInternalSensors()) {
			mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);
			List<Sensors> intSens = sd.getInternalSensors();
			List<String> intSensNames = new ArrayList<String>();
			List<Sensor> selectedSensors = new ArrayList<Sensor>();
			for (Sensors sen : intSens) {
				intSensNames.add(sen.getSensorName());
			}
			for (Sensor sen : sensors) {
				if (intSensNames.contains(sen.getName())) {
					selectedSensors.add(sen);
				}
			}
			internalSensors = new mSensors(selectedSensors, mgr);
			for (mSensor sen : internalSensors.getAllSensors()) {
				mgr.registerListener(this, sen.getSensor(),
						SensorManager.SENSOR_DELAY_NORMAL);
			}
		}

		// uspostava veze sa vanjskim usb senzorom
		if (sd.containsExternalUsbSensors()) {
			usbDeviceContext = getApplicationContext();
			try {
				ftdid2xx = D2xxManager.getInstance(usbDeviceContext);
			} catch (D2xxException e) {
				e.printStackTrace();
			}
			connectToUsb();
	
			configureUart();
			//Toast.makeText(this, "ftDev = " + ftDev, Toast.LENGTH_SHORT).show();
		} else {
			closeUsbDevice();
		}

		if (!sd.containsExternalBluetoothSensors()) {
			doWork();
		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(null);
		handler.removeCallbacksAndMessages(null);
		if (inStream != null) {
			try {
				inStream.close();
			} catch (Exception ex) {

			}
		}
		if (outStream != null) {
			try {
				outStream.close();
			} catch (Exception ex) {

			}
		}
		if (connectionSocket != null) {
			try {
				connectionSocket.close();
			} catch (Exception ex) {

			}
			connectionSocket = null;
		}

		closeUsbDevice();

		Log.w("SensorMeasurementService", "Service killed");
		super.onDestroy();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		mSensor changedValueSensor = internalSensors
				.getSensorByName(event.sensor.getName());
		if (changedValueSensor != null) {
			changedValueSensor.setSensorValue(event.values[0]);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void doWork() {
		ts = System.currentTimeMillis();
		if (sd.containsInternalSensors()) {
			if (!first) {
				for (mSensor sen : internalSensors.getAllSensors()) {
					int id = sd.getSensorIdByName(sen.getSensorName());
					if (id != -1) {
						sensorMeasurementData.addSensorMeasurement(id, ts,
								String.valueOf(sen.getSensorValue()), "N/A");
						Log.d("SensorMeasurementService to database",
								"id: " + String.valueOf(id) + " ts: "
										+ String.valueOf(ts) + " value: "
										+ String.valueOf(sen.getSensorValue()));
					}
				}
			}
			first = false;
		}
		if (sd.containsExternalBluetoothSensors()) {
			// byte[] buffer = new byte[1024];
			// buffer = "Data".getBytes();
			// try
			// {
			// outStream.write(buffer);
			// outStream.flush();
			// }
			// catch(Exception ex)
			// {
			// Log.e("SensorMeasurementService", ex.getMessage());
			// }
			byte[] readBuff = new byte[1024];
			try {
				// int timeout = 0;
				// int maxTimeout = 8;
				// while(inStream.available() == 0 && timeout < maxTimeout)
				// {
				// timeout++;
				// Thread.sleep(250);
				// }
				int timeout = 0;
				int maxTimeout = 40;
				// inStream.skip(inStream.available());
				while (inStream.available() != 0) {
					inStream.skip(inStream.available());
				}
				while (inStream.available() == 0 && timeout < maxTimeout) {
					timeout++;
					Thread.sleep(50);
				}
				inStream.read(readBuff);
				String dataReceived = new String(readBuff).trim();
				int id = sd.getSensorIdByName(sd.getExternalBluetoothSensors()
						.get(0).getSensorName());
				if (id != -1) {
					sensorMeasurementData.addSensorMeasurement(id, ts,
							dataReceived, "N/A");
					Log.d("SensorMeasurementService to database BT", "id: "
							+ String.valueOf(id) + " ts: " + String.valueOf(ts)
							+ " value: " + dataReceived);
				}
			} catch (Exception ex) {
				Log.e("SensorMeasurementService", ex.getMessage());
			}
		}
		// procitaj podatak i zapisi ga u bazu
		if (sd.containsExternalUsbSensors()) {

			if (ftDev.isOpen()) {
				// Toast.makeText(getApplicationContext(), "pocetak: " + times +
				// saved, Toast.LENGTH_SHORT).show();
			} else {
				// Toast.makeText(getApplicationContext(), "ponovno",
				// Toast.LENGTH_SHORT).show();
				connectToUsb();
			}
			read_thread = new readThread();
			bReadThreadGoing = true;
			read_thread.start();

			/*
			 * iavailable = ftDev.getQueueStatus();
			 * Toast.makeText(getApplicationContext(), "que = " + iavailable +
			 * "", Toast.LENGTH_SHORT).show(); if (iavailable > 0) { if
			 * (iavailable > readLength) { iavailable = readLength; }
			 * //Toast.makeText(getApplicationContext(), "read = " + ,
			 * Toast.LENGTH_SHORT).show(); ftDev.read(readData, iavailable); for
			 * (i = 0; i < iavailable; i++) { readDataToText[i] = (char)
			 * readData[i]; } String data = String.copyValueOf(readDataToText,
			 * 0, iavailable); Toast.makeText(getApplicationContext(), "" +
			 * data, Toast.LENGTH_SHORT).show(); }
			 */
		}

		// na ovaj nacin metoda rekurzivno poziva sama sebe po svom zavrsetku
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				doWork();
			}
			// TODO PROMIJENI NA TimeUnit.MINUTES.toMillis(period));
		}, TimeUnit.SECONDS.toMillis(5));
	}

	public void MakeConnection() {
		if (sensAddress.isEmpty()) {
		} else {
			sensorDevice = bluetooth.getRemoteDevice(sensAddress);
			UUID uuid = sensorDevice.getUuids()[0].getUuid();
			try {
				connectionSocket = sensorDevice
						.createInsecureRfcommSocketToServiceRecord(uuid);
				bluetooth.cancelDiscovery();
				connectionSocket.connect();
				inStream = connectionSocket.getInputStream();
				outStream = connectionSocket.getOutputStream();
			} catch (Exception ex) {
				Log.e("SensorMeasurementService", ex.getMessage());
			}
		}
	}

	/**
	 * Otvara vezu prema prvom spojenom usb uredjaju i registrira listener za
	 * akciju iskapcanja usb uredjaja koja ga deregistrira.
	 */
	private void connectToUsb() {
		DevCount = ftdid2xx.createDeviceInfoList(usbDeviceContext);
		if (null == ftDev & DevCount > 0) {
			ftDev = ftdid2xx.openByIndex(usbDeviceContext, 0);
		}
		if (ftDev != null) {
			ftDevId = sd.getExternalUsbSensors().get(0).get_id();
			connected = true;
			ftDev.purge((D2xxManager.FT_PURGE_TX));
			ftDev.restartInTask();
			registerReceiver(mUsbReceiver, new IntentFilter(
					UsbManager.ACTION_USB_DEVICE_DETACHED));
		}
	}

	/**
	 * namjesat postavke usb sucelja tako da je moguce procitati sto se prima
	 */
	private void configureUart() {
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDev.setBaudRate(38400);
		ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
				D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
		ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x0b, (byte) 0x0d);
		uart_configured = true;
	}

	/**
	 * Trenutno zatvara vezu s usbom ako postoji, brise ftDev referencu,
	 * postalja odgovarajuce varijable i deregistrira receiver za iskapcanje
	 * usba.
	 */
	private void closeUsbDevice() {
		DevCount = 0;
		bReadThreadGoing = false;

		if (ftDev != null) {
			synchronized (ftDev) {
				if (true == ftDev.isOpen()) {
					Toast.makeText(this, "Closing usb connection",
							Toast.LENGTH_SHORT).show();
					ftDev.close();
					ftDev = null;
					unregisterReceiver(mUsbReceiver);
				}
			}
		}
		connected = false;
		uart_configured = false;
	}

	/**
	 * Receiver koji registrira otkapčanje USB uređaja.
	 */
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				closeUsbDevice();
			}
		}
	};

	/**
	 * Dretva koja u pozadini čita podatke sa spojenog usb uređaja i iste
	 * podatke sprema u bazu pod timestampom ts.
	 */
	private class readThread extends Thread {
		// Handler mHandler;
		Handler uiHandler = new Handler(Looper.getMainLooper());

		readThread() {
			// mHandler = h;
			this.setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			int i;

			while (true == bReadThreadGoing & connected) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}

				synchronized (ftDev) {
					iavailable = ftDev.getQueueStatus();
					if (iavailable > 0 & uart_configured) {

						if (iavailable > readLength) {
							iavailable = readLength;
						}

						ftDev.read(readData, iavailable);
						for (i = 0; i < iavailable; i++) {
							readDataToText[i] = (char) readData[i];
						}
						/*
						 * TODO otkriti zasto nece ispisati data nego ispisuje
						 * prazan string, a u drugoj dretvi normalno ispise
						 * sadrzaj reference data
						 */
						data = String
								.copyValueOf(readDataToText, 0, iavailable);
						// Toast.makeText(getApplicationContext(), "procitao: "
						// + data, Toast.LENGTH_SHORT).show();

						uiHandler.post(new Runnable() {

							@Override
							public void run() {

								data = data.trim();
								data = data.split("!moteid!")[2];
								data = data.trim();
								if (data.endsWith("!end!")) {
									data = data.split("!end!")[0];
									WaspmoteApplication waspApp = (WaspmoteApplication) getApplication();
									SensorMeasurementDataSource sensorMeasurementData = (SensorMeasurementDataSource) waspApp
											.getWaspmoteSqlHelper()
											.getSensorMeasurementDataSource(
													SensorMeasurementService.this);
									sensorMeasurementData.addSensorMeasurement(
											ftDevId, ts, data, "N/A");
									// Toast.makeText(getApplicationContext(),
									// data, Toast.LENGTH_SHORT).show();
								}
							}
						});
						bReadThreadGoing = false;
					}
				}
			}
		}

	}

}
