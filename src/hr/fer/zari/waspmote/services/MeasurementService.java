package hr.fer.zari.waspmote.services;

import hr.fer.zari.waspmote.WaspmoteApplication;
import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;

/**
 * Zadatak ovog servisa je očitavati podatke sa senzora po definiranim
 * kriterijima, te ih zapisivati u bazu.Ovaj servis je striktno razvijen za
 * prikupljanje podataka s usb senzora i trenutno nije u uporabi. Trenutno se
 * koristi samo servis SensorMeasurementsService za očitavanje i spremanje
 * podataka.
 * <p>
 * Servis radi tako da pokrene zasebnu dretvu koja periodički čita podatke s usb
 * sučelja i putem servisovog handler objekta šalje tom servisu prazne poruke. Servisov handler reagira na poruke tako da pročita broj podataka na ulazu koji je odredila pozadinska dretva i 
 * </p>
 * 
 * @author Igor Petkovski
 * @version 1.0
 * 
 */
public class MeasurementService extends Service {

	Handler mHandler;
	private Context usbDeviceContext;
	private D2xxManager ftdid2xx;
	private FT_Device ftDev = null;
	private int ftDevId;
	private int openIndex;
	private int currentIndex = -2;
	private int sleepTime = 1000;

	/* read thread variables */
	public static final int readLength = 512;
	public int readcount = 0;
	public int iavailable = 0;
	byte[] readData;
	char[] readDataToText;
	public boolean bReadThreadGoing = false;
	public readThread read_thread;

	boolean uart_configured = false;
	boolean idIsConfigured = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		usbDeviceContext = getApplicationContext();
		readData = new byte[readLength];
		readDataToText = new char[readLength];

		try {
			ftdid2xx = D2xxManager.getInstance(usbDeviceContext);
		} catch (D2xxException e) {
			e.printStackTrace();
		}

		IntentFilter filter = new IntentFilter(
				UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Toast.makeText(this, "USB service started ", Toast.LENGTH_SHORT).show();
		mHandler = new Handler();
		connectFunction();

		// this.stopSelf(startId);
		return START_STICKY;

	}

	@Override
	public void onDestroy() {
		bReadThreadGoing = false;
		unregisterReceiver(mUsbReceiver);
		closeUsbDevice();
		super.onDestroy();
	}

	/**
	 * Spaja se na USB uređaj, ispisuje odgovarajuću poruku u slučaju neuspjeha
	 * i pokreće pozadinsku dretvu za čitanje s USB-a.
	 */
	public void connectFunction() {

		int tmpProtNumber = openIndex + 1;

		if (currentIndex != openIndex) {
			if (null == ftDev) {
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, openIndex);
			}
			uart_configured = false;
		} else {
			Toast.makeText(usbDeviceContext,
					"S Device port " + tmpProtNumber + " is already opened",
					Toast.LENGTH_LONG).show();
			return;
		}

		if (ftDev == null) {
			Toast.makeText(
					usbDeviceContext,
					"S open device port(" + tmpProtNumber
							+ ") NG, ftDev == null", Toast.LENGTH_LONG).show();
			return;
		}

		if (true == ftDev.isOpen()) {
			currentIndex = openIndex;
			Toast.makeText(usbDeviceContext,
					"S open device port(" + tmpProtNumber + ") OK",
					Toast.LENGTH_SHORT).show();

			// config port
			ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

			ftDev.setBaudRate(38400);
			ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
					D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
			ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x0b,
					(byte) 0x0d);
			uart_configured = true;

			// start read thread
			if (false == bReadThreadGoing) {
				read_thread = new readThread(handler);
				bReadThreadGoing = true;
				read_thread.start();
				Toast.makeText(this, "S read thread started ",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(usbDeviceContext,
					"S open device port(" + tmpProtNumber + ") NG",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Handler koji prima poruke iz pozadinske dretve za čitanje i zapisuje
	 * primljenje podatke u bazu podataka.
	 */
	Handler handler = new Handler() {

		WaspmoteApplication waspApp;
		SensorMeasurementDataSource sensorMeasurementData;

		@Override
		public void handleMessage(Message msg) {
			if (iavailable > 0) {
				if (idIsConfigured) {
					waspApp = (WaspmoteApplication) getApplication();
					sensorMeasurementData = (SensorMeasurementDataSource) waspApp
							.getWaspmoteSqlHelper()
							.getSensorMeasurementDataSource(
									MeasurementService.this);
					String data = String.copyValueOf(readDataToText, 0,
							iavailable);

					data = data.split("!moteid!")[2];
					data = data.trim();
					if (data.endsWith("!end!")) {
						data = data.replace("!end!", " ");
						data = data.trim();
					}
					// Toast.makeText(getApplicationContext(), data,
					// Toast.LENGTH_SHORT).show();
					sensorMeasurementData.addSensorMeasurement(ftDevId,
							System.currentTimeMillis(), data, "N/A");
				} else {
					configureId(String.copyValueOf(readDataToText, 0,
							iavailable));
				}

			}
		}

	};

	/**
	 * Closes opened ftDev.
	 */
	private void closeUsbDevice() {
		currentIndex = -2;
		bReadThreadGoing = false;

		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException ignore) {
		}

		if (ftDev != null) {
			synchronized (ftDev) {
				if (true == ftDev.isOpen()) {

					Toast.makeText(this, "S Closing usb", Toast.LENGTH_SHORT)
							.show();
					ftDev.close();
				}
				ftDev = null;
			}

		}
	}

	/**
	 * Receiver koji registrira otkapčanje USB uređaja. Zaustavlja pozadinsku
	 * dretvu i izlazi iz activitija.
	 */
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Toast.makeText(MeasurementService.this, "SB Gasim servis",
						Toast.LENGTH_SHORT).show();
				bReadThreadGoing = false;
				closeUsbDevice();
				stopSelf();
			}
		}

	};

	private class readThread extends Thread {
		Handler threadHandler;

		readThread(Handler h) {
			threadHandler = h;
			this.setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			int i;
			ftDev.purge((D2xxManager.FT_PURGE_TX));
			ftDev.restartInTask();

			Message msg = threadHandler.obtainMessage();
			threadHandler.sendMessage(msg);

			while (true == bReadThreadGoing) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ignore) {
				}

				synchronized (ftDev) {
					iavailable = ftDev.getQueueStatus();
					if (iavailable > 0) {
						if (iavailable > readLength) {
							iavailable = readLength;
						}
						ftDev.read(readData, iavailable);
						for (i = 0; i < iavailable; i++) {
							readDataToText[i] = (char) readData[i];
						}
						msg = threadHandler.obtainMessage();
						threadHandler.sendMessage(msg);
					}
				}
			}
		}
	}

	private void configureId(String sensorName) {
		if ((sensorName.length() > 25) & sensorName.startsWith("!moteid!")) {

			sensorName = sensorName.substring(8);
			sensorName = sensorName.substring(0, sensorName.indexOf('!'));

			WaspmoteApplication waspApp = (WaspmoteApplication) getApplication();
			SensorsDataSource sensors = (SensorsDataSource) waspApp
					.getWaspmoteSqlHelper().getSensorsDataSource(this);

			if (!sensors.SensorExists(sensorName)) {
				sensors.addSensor(sensorName, 3);
				Toast.makeText(this,
						"Sensor: " + sensorName + " is NOT registered!",
						Toast.LENGTH_SHORT).show();
			} else {
				ftDevId = sensors.getSensorIdByName(sensorName);
				Toast.makeText(this, "Sensor id is: " + ftDevId + " !",
						Toast.LENGTH_SHORT).show();
			}
			idIsConfigured = true;
		}
	}

}
