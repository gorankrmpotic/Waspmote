package hr.fer.zari.waspmote.services;

import hr.fer.zari.waspmote.WaspmoteApplication;
import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
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
 * kriterijima, te ih zapisivati u bazu. Ovaj servis jednom pokrenut ostaje
 * upogonjen neovisno o UI dretvi aplikacije.
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
	private int openIndex;
	private int currentIndex = -2;

	/* read thread variables */
	public static final int readLength = 512;
	public int readcount = 0;
	public int iavailable = 0;
	byte[] readData;
	char[] readDataToText;
	public boolean bReadThreadGoing = false;
	public readThread read_thread;

	boolean uart_configured = false;

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
	 * Spaja se na USB uređaj, ispisuje odgovarajuću poruku u slučaju
	 * neuspjeha i pokreće pozadinsku dretvu za čitanje s USB-a.
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
				read_thread.start();
				Toast.makeText(this, "S read thread started ",
						Toast.LENGTH_SHORT).show();

				bReadThreadGoing = true;
			}

		} else {
			Toast.makeText(usbDeviceContext,
					"S open device port(" + tmpProtNumber + ") NG",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Handler koji prima poruke iz pozadinske dretve za čitanje i
	 * zapisuje primljenje podatke u bazu podataka.
	 */
	Handler handler = new Handler() {

		WaspmoteApplication waspApp;
		SensorMeasurementDataSource sensorMeasurementData;
		
		@Override
		public void handleMessage(Message msg) {
			
			//Toast.makeText(MeasurementService.this,	"S handler: " + String.valueOf(msg.arg1),Toast.LENGTH_SHORT).show();

			if (iavailable > 0) {
				//Toast.makeText(MeasurementService.this,	String.copyValueOf(readDataToText, 0, iavailable),
				//		Toast.LENGTH_SHORT).show();
				waspApp = (WaspmoteApplication)getApplication();
				sensorMeasurementData = (SensorMeasurementDataSource) waspApp.getWaspmoteSqlHelper().getSensorMeasurementDataSource(MeasurementService.this);		
				
				sensorMeasurementData.addSensorMeasurement(1, System.currentTimeMillis(), (String)msg.obj, "N/A");
			}	
		}

	};

	/**
	 * Closes opened ftDev.
	 */
	private void closeUsbDevice() {
		currentIndex = -2;
		bReadThreadGoing = false;

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

			while (true == bReadThreadGoing) {
				try {
					Thread.sleep(500);
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
						Message msg = threadHandler.obtainMessage();
						threadHandler.sendMessage(msg);
					}
				}
			}
		}

	}

}
