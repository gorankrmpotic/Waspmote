package hr.fer.zari.waspmote;

<<<<<<< HEAD
=======
import java.util.ArrayList;

import android.content.BroadcastReceiver;
>>>>>>> origin/master
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;

/**
 * Activity koji omogućuje čitanje/pisanje podataka na spojeni USB uređaj. Spaja
 * se na index uređaja koji je kliknut u prethodnoj listi spojenih usb uređaja.
 * <p>
 * Prilikom izlaska uništava sve ostvarene veze sa spojenim uređajem i tako
 * omogućava nesmetani rad ostatka aplikacije.
 * </p>
 * 
 * @author Igor Petkovski
 * 
 */
public class ViewUsbSensorDataActivity extends ActionBarActivity {

	private static final String TAG = ViewUsbSensorDataActivity.class.getSimpleName();

	private Context usbDeviceContext;
	private D2xxManager ftdid2xx;
	private FT_Device ftDev = null;
	private int clickedUsbIndex;
	private int DevCount = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_usb_sensor_data);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			clickedUsbIndex = 0;
		} else {
			Toast.makeText(this, "has extras", Toast.LENGTH_SHORT).show();
			clickedUsbIndex = extras.getInt("ClickedUsbIndex", 0);
			Toast.makeText(this, "clicked index: " + String.valueOf(clickedUsbIndex), Toast.LENGTH_SHORT).show();
		}
		
		usbDeviceContext = getApplicationContext();
		
		try {
			ftdid2xx = D2xxManager.getInstance(usbDeviceContext);
		} catch (D2xxException e) {
			e.printStackTrace();
		}
<<<<<<< HEAD
		
		if (ftdid2xx == null) {
			Toast.makeText(this, "Manager is null", Toast.LENGTH_SHORT).show();
		}
		
		//closeUsbDevice();
		createDeviceList();
		
		//Toast.makeText(this, "Dev count: " + String.valueOf(DevCount), Toast.LENGTH_SHORT).show();
		
		if (null == ftDev) {
			if (DevCount > 0) {
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, DevCount - 1);
				Toast.makeText(this, "DevCount = " + DevCount, Toast.LENGTH_SHORT).show();
				if (ftDev != null) {
					Toast.makeText(this, ftDev.getDeviceInfo().serialNumber, Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "No devices found!", Toast.LENGTH_SHORT)
						.show();
				onBackPressed();
			}
		} else {
			synchronized (ftDev) {
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, DevCount - 1);
				Toast.makeText(this, "Was connected previously.", Toast.LENGTH_SHORT).show();
			}
		}
=======

		IntentFilter filter = new IntentFilter(
				UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		/*
		 * if (null == ftDev) { if (DevCount > 0) { ftDev =
		 * ftdid2xx.openByIndex(usbDeviceContext, DevCount - 1); if (ftDev !=
		 * null) { Toast.makeText(this, ftDev.getDeviceInfo().serialNumber,
		 * Toast.LENGTH_SHORT).show(); } } else { Toast.makeText(this,
		 * "No devices found!", Toast.LENGTH_SHORT) .show(); onBackPressed(); }
		 * } else { synchronized (ftDev) { ftDev =
		 * ftdid2xx.openByIndex(usbDeviceContext, DevCount - 1);
		 * Toast.makeText(this, "Was connected previously.",
		 * Toast.LENGTH_SHORT).show(); } }
		 */

		// * Inflate layout *
		readData = new byte[readLength];
		readDataToText = new char[readLength];

		readText = (EditText) findViewById(R.id.ReadValues);
		readText.setInputType(0);
		writeText = (EditText) findViewById(R.id.WriteValues);
>>>>>>> origin/master

		
	}

<<<<<<< HEAD

	@Override
	protected void onStop() {
		closeUsbDevice();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
=======
	/* Implements all options listeners */

	/**
	 * Checks the number of connected devices and sets the appropriate spinner
	 * string list for port selector option.
	 */
	public void updatePortNumberSelector() {
		// Toast.makeText(DeviceUARTContext, "updatePortNumberSelector:" +
		// DevCount, Toast.LENGTH_SHORT).show();

		if (DevCount == 2) {
			portAdapter = ArrayAdapter.createFromResource(usbDeviceContext,
					R.array.port_list_2, R.layout.my_spinner_textview);
			portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
			portSpinner.setAdapter(portAdapter);
			portAdapter.notifyDataSetChanged();
			Toast.makeText(usbDeviceContext, "2 port device attached",
					Toast.LENGTH_SHORT).show();
			// portSpinner.setOnItemSelectedListener(new
			// MyOnPortSelectedListener());
		} else if (DevCount == 4) {
			portAdapter = ArrayAdapter.createFromResource(usbDeviceContext,
					R.array.port_list_4, R.layout.my_spinner_textview);
			portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
			portSpinner.setAdapter(portAdapter);
			portAdapter.notifyDataSetChanged();
			Toast.makeText(usbDeviceContext, "4 port device attached",
					Toast.LENGTH_SHORT).show();
			// portSpinner.setOnItemSelectedListener(new
			// MyOnPortSelectedListener());
		} else {
			portAdapter = ArrayAdapter.createFromResource(usbDeviceContext,
					R.array.port_list_1, R.layout.my_spinner_textview);
			portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
			portSpinner.setAdapter(portAdapter);
			portAdapter.notifyDataSetChanged();
			Toast.makeText(usbDeviceContext, "1 port device attached",
					Toast.LENGTH_SHORT).show();
			// portSpinner.setOnItemSelectedListener(new
			// MyOnPortSelectedListener());
		}

	}

	public class MyOnBaudSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			baudRate = Integer.parseInt(parent.getItemAtPosition(pos)
					.toString());
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	public class MyOnStopSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			stopBit = (byte) Integer.parseInt(parent.getItemAtPosition(pos)
					.toString());
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	public class MyOnDataSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			dataBit = (byte) Integer.parseInt(parent.getItemAtPosition(pos)
					.toString());
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	public class MyOnParitySelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String parityString = new String(parent.getItemAtPosition(pos)
					.toString());
			if (parityString.compareTo("none") == 0) {
				parity = 0;
			} else if (parityString.compareTo("odd") == 0) {
				parity = 1;
			} else if (parityString.compareTo("even") == 0) {
				parity = 2;
			} else if (parityString.compareTo("mark") == 0) {
				parity = 3;
			} else if (parityString.compareTo("space") == 0) {
				parity = 4;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	public class MyOnFlowSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String flowString = new String(parent.getItemAtPosition(pos)
					.toString());
			if (flowString.compareTo("none") == 0) {
				flowControl = 0;
			} else if (flowString.compareTo("CTS/RTS") == 0) {
				flowControl = 1;
			} else if (flowString.compareTo("DTR/DSR") == 0) {
				flowControl = 2;
			} else if (flowString.compareTo("XOFF/XON") == 0) {
				flowControl = 3;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	public class MyOnPortSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			openIndex = Integer.parseInt(parent.getItemAtPosition(pos)
					.toString()) - 1;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity,
			byte flowControl) {
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SetConfig: device not open");
			return;
		}

		// configure our port
		// reset to UART mode for 232 devices
		ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

		ftDev.setBaudRate(baud);

		switch (dataBits) {
		case 7:
			dataBits = D2xxManager.FT_DATA_BITS_7;
			break;
		case 8:
			dataBits = D2xxManager.FT_DATA_BITS_8;
			break;
		default:
			dataBits = D2xxManager.FT_DATA_BITS_8;
			break;
		}

		switch (stopBits) {
		case 1:
			stopBits = D2xxManager.FT_STOP_BITS_1;
			break;
		case 2:
			stopBits = D2xxManager.FT_STOP_BITS_2;
			break;
		default:
			stopBits = D2xxManager.FT_STOP_BITS_1;
			break;
		}

		switch (parity) {
		case 0:
			parity = D2xxManager.FT_PARITY_NONE;
			break;
		case 1:
			parity = D2xxManager.FT_PARITY_ODD;
			break;
		case 2:
			parity = D2xxManager.FT_PARITY_EVEN;
			break;
		case 3:
			parity = D2xxManager.FT_PARITY_MARK;
			break;
		case 4:
			parity = D2xxManager.FT_PARITY_SPACE;
			break;
		default:
			parity = D2xxManager.FT_PARITY_NONE;
			break;
		}

		ftDev.setDataCharacteristics(dataBits, stopBits, parity);

		short flowCtrlSetting;
		switch (flowControl) {
		case 0:
			flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
			break;
		case 1:
			flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
			break;
		case 2:
			flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
			break;
		case 3:
			flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
			break;
		default:
			flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
			break;
		}

		// TODO : flow ctrl: XOFF/XOM
		// TODO : flow ctrl: XOFF/XOM
		ftDev.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);

		uart_configured = true;
		Toast.makeText(usbDeviceContext, "Config done", Toast.LENGTH_SHORT)
				.show();
>>>>>>> origin/master
	}

	/* Life cycle methods */

	/**
	 * Samo pobroji spojene uređaje prilikom svakog pokretanja aktivitija.
	 */
	@Override
	public void onResume() {
		super.onResume();
		DevCount = 0;
		createDeviceList();

		// Toast.makeText(this, "onResume,  devc: " + DevCount,
		// Toast.LENGTH_SHORT).show();
		/*
		 * if(DevCount > 0) { connectFunction(); SetConfig(baudRate, dataBit,
		 * stopBit, parity, flowControl); }
		 */
	}

	/**
	 * Zaustavlja pozadinsku dretvu i odspaja se od USB-a.
	 */
	@Override
	protected void onStop() {
		if (bReadThreadGoing) {
			bReadThreadGoing = false;
		}
		closeUsbDevice();
		super.onStop();
	}

	/**
	 * Zaustavlja pozadinsku dretvu i odspaja se od USB-a. Deregistrira
	 * broadcast receiver za USB_DEVICE_DETACHED događaj.
	 */
	@Override
	protected void onPause() {
		if (bReadThreadGoing) {
			bReadThreadGoing = false;
		}
		unregisterReceiver(mUsbReceiver);
		closeUsbDevice();
		super.onPause();
	}

	/**
	 * Metoda samo postavlja broj trenutno spojenih uređaja.
	 */
	private void createDeviceList() {
		int tempDevCount = ftdid2xx.createDeviceInfoList(usbDeviceContext);

		if (tempDevCount > 0) {
			if (DevCount != tempDevCount) {
				DevCount = tempDevCount;
			}
		} else {
			DevCount = -1;
		}
	}
<<<<<<< HEAD
=======

	/**
	 * Spaja se na prvi USB uređaj, i ispisuje odgovarajuću poruku u slučaju
	 * neuspjeha.
	 */
	public void connectFunction() {
		int tmpProtNumber = openIndex + 1;
		Toast.makeText(this,
				"current: " + currentIndex + "  \nopenIndex: " + openIndex,
				Toast.LENGTH_SHORT).show();
		if (currentIndex != openIndex) {
			if (null == ftDev) {
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, openIndex);
				Toast.makeText(this, "ftDev = " + ftDev, Toast.LENGTH_SHORT)
						.show();
			} else {
				synchronized (ftDev) {
					Toast.makeText(this, "Sxnchtonised part entered.",
							Toast.LENGTH_SHORT).show();
					ftDev = ftdid2xx.openByIndex(usbDeviceContext, openIndex);
				}
			}
			uart_configured = false;
		} else {
			Toast.makeText(usbDeviceContext,
					"Device port " + tmpProtNumber + " is already opened",
					Toast.LENGTH_LONG).show();
			return;
		}

		if (ftDev == null) {
			Toast.makeText(
					usbDeviceContext,
					"open device port(" + tmpProtNumber + ") NG, ftDev == null",
					Toast.LENGTH_LONG).show();
			return;
		}

		if (true == ftDev.isOpen()) {
			currentIndex = openIndex;
			Toast.makeText(usbDeviceContext,
					"open device port(" + tmpProtNumber + ") OK",
					Toast.LENGTH_SHORT).show();

			if (false == bReadThreadGoing) {
				read_thread = new readThread(handler);
				read_thread.start();
				bReadThreadGoing = true;
			}
		} else {
			Toast.makeText(usbDeviceContext,
					"open device port(" + tmpProtNumber + ") NG",
					Toast.LENGTH_LONG).show();
			// Toast.makeText(DeviceUARTContext, "Need to get permission!",
			// Toast.LENGTH_SHORT).show();
		}
	}
>>>>>>> origin/master

	/**
	 * Closes opened ftDev.
	 */
	private void closeUsbDevice() {
		DevCount = -1;
<<<<<<< HEAD
		if (ftDev != null) {
			if (ftDev.isOpen()) {
				Toast.makeText(this, "Closing usb, error", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Closing usb device connection.");
				ftDev.close();
			}
		}
	}
=======
		currentIndex = -2;
		bReadThreadGoing = false;

		if (ftDev != null) {
			synchronized (ftDev) {
				if (true == ftDev.isOpen()) {
					Toast.makeText(this, "Closing usb", Toast.LENGTH_SHORT)
							.show();
					Log.d(TAG, "Closing usb device connection.");
					ftDev.close();
				}
			}
		}
	}

	/**
	 * Toggle funkcija koja ne/omogućava čitanje sa spojenog usb uređaja,
	 * odnosno ažuriranje podataka u prozor Read Bytes.
	 */
	public void EnableRead() {
		iEnableReadFlag = (iEnableReadFlag + 1) % 2;

		if (iEnableReadFlag == 1) {
			ftDev.purge((D2xxManager.FT_PURGE_TX));
			ftDev.restartInTask();
			readEnButton.setText("Read Enabled");
		} else {
			ftDev.stopInTask();
			readEnButton.setText("Read Disabled");
		}
	}

	/**
	 * Funkcija koja šalje podatke na spojeni USB uređaj.
	 */
	public void SendMessage() {
		if (ftDev.isOpen() == false) {
			Log.e("j2xx", "SendMessage: device not open");
			return;
		}

		ftDev.setLatencyTimer((byte) 16);
		// ftDev.purge((byte) (D2xxManager.FT_PURGE_TX |
		// D2xxManager.FT_PURGE_RX));

		String writeData = writeText.getText().toString();
		byte[] OutData = writeData.getBytes();
		ftDev.write(OutData, writeData.length());
	}

	/**
	 * Handler koji će primati poruke iz pozadinske dretve za čitanje i
	 * ažurirati prozor Read Bytes.
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (iavailable > 0) {
				readText.append(String.copyValueOf(readDataToText, 0,
						iavailable));
			}
		}
	};

	/**
	 * Dretva koja u pozadini čita podatke sa spojenog usb uređaja i iste
	 * podatke prikazuje u prozoru 'Read Bytes'.
	 * 
	 * @author Oberon
	 * 
	 */
	private class readThread extends Thread {
		Handler mHandler;

		readThread(Handler h) {
			mHandler = h;
			this.setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			int i;

			while (true == bReadThreadGoing) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
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
						Message msg = mHandler.obtainMessage();
						mHandler.sendMessage(msg);
					}
				}
			}

			synchronized (ftDev) {
				if (ftDev.isOpen()) {

					closeUsbDevice();
					ViewUsbSensorDataActivity.this
							.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(
											getApplicationContext(),
											"Zovem usb close iz dretve: "
													+ Thread.currentThread()
															.getName(),
											Toast.LENGTH_SHORT).show();
									closeUsbDevice();
									currentIndex = -2;
								}
							});
				}
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
				Toast.makeText(ViewUsbSensorDataActivity.this,
						"Device detatched", Toast.LENGTH_SHORT).show();
				bReadThreadGoing = false;
				ftDev = null;
				onBackPressed();
			}
		}
	};
>>>>>>> origin/master
}
