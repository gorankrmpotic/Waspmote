package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.services.MeasurementService;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

	private static final String TAG = ViewUsbSensorDataActivity.class
			.getSimpleName();

	private Context usbDeviceContext;
	private D2xxManager ftdid2xx;
	private FT_Device ftDev = null;
	private int openIndex;
	private int currentIndex = -2;
	private int DevCount = 0;

	/* graphical objects */
	EditText readText;
	EditText writeText;
	Spinner baudSpinner;;
	Spinner stopSpinner;
	Spinner dataSpinner;
	Spinner paritySpinner;
	Spinner flowSpinner;
	Spinner portSpinner;
	ArrayAdapter<CharSequence> portAdapter;

	Button configButton;
	Button openButton;
	Button readEnButton;
	Button writeButton;
	static int iEnableReadFlag = 1;

	/* local variables */
	int baudRate; /* baud rate */
	byte stopBit; /* 1:1stop bits, 2:2 stop bits */
	byte dataBit; /* 8:8bit, 7: 7bit */
	byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
	byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */
	int portNumber; /* port number */
	ArrayList<CharSequence> portNumberList;

	/* read thread variables */
	public static final int readLength = 512;
	public int readcount = 0;
	public int iavailable = 0;
	byte[] readData;
	char[] readDataToText;
	public boolean bReadThreadGoing = false;
	public readThread read_thread;

	boolean uart_configured = false;
	boolean isRegistered = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_usb_sensor_data);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			openIndex = 0;
		} else {
			openIndex = extras.getInt("ClickedUsbIndex", 0);
		}

		usbDeviceContext = getApplicationContext();

		try {
			ftdid2xx = D2xxManager.getInstance(usbDeviceContext);
		} catch (D2xxException e) {
			e.printStackTrace();
		}

		// zovem servis za pracenje usb podataka ako vec nije pokrenut
		/*
		if (isMyServiceRunning(MeasurementService.class)) {
			Toast.makeText(this, "USB service running", Toast.LENGTH_SHORT)
					.show();
		} else {
			if (getIntent().getAction() != null) {
				if (getIntent()
						.getAction()
						.equals(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
					startService(new Intent(this, MeasurementService.class));
				}
			} else {
				createDeviceList();
				if (DevCount > 0) {
					startService(new Intent(this, MeasurementService.class));
				}
			}
		}
		*/

		// * Inflate layout *
		readData = new byte[readLength];
		readDataToText = new char[readLength];

		readText = (EditText) findViewById(R.id.ReadValues);
		readText.setInputType(0);
		writeText = (EditText) findViewById(R.id.WriteValues);

		openButton = (Button) findViewById(R.id.openButton);
		configButton = (Button) findViewById(R.id.configButton);
		readEnButton = (Button) findViewById(R.id.readEnButton);
		writeButton = (Button) findViewById(R.id.WriteButton);

		baudSpinner = (Spinner) findViewById(R.id.baudRateValue);
		ArrayAdapter<CharSequence> baudAdapter = ArrayAdapter
				.createFromResource(usbDeviceContext, R.array.baud_rate,
						R.layout.my_spinner_textview);
		baudAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		baudSpinner.setAdapter(baudAdapter);
		baudSpinner.setSelection(6);
		/* by default it is 38400 */
		baudRate = 38400;

		stopSpinner = (Spinner) findViewById(R.id.stopBitValue);
		ArrayAdapter<CharSequence> stopAdapter = ArrayAdapter
				.createFromResource(usbDeviceContext, R.array.stop_bits,
						R.layout.my_spinner_textview);
		stopAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		stopSpinner.setAdapter(stopAdapter);
		/* default is stop bit 1 */
		stopBit = 1;

		dataSpinner = (Spinner) findViewById(R.id.dataBitValue);
		ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter
				.createFromResource(usbDeviceContext, R.array.data_bits,
						R.layout.my_spinner_textview);
		dataAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		dataSpinner.setAdapter(dataAdapter);
		dataSpinner.setSelection(1);
		/* default data bit is 8 bit */
		dataBit = 8;

		paritySpinner = (Spinner) findViewById(R.id.parityValue);
		ArrayAdapter<CharSequence> parityAdapter = ArrayAdapter
				.createFromResource(usbDeviceContext, R.array.parity,
						R.layout.my_spinner_textview);
		parityAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		paritySpinner.setAdapter(parityAdapter);
		/* default is none */
		parity = 0;

		flowSpinner = (Spinner) findViewById(R.id.flowControlValue);
		ArrayAdapter<CharSequence> flowAdapter = ArrayAdapter
				.createFromResource(usbDeviceContext, R.array.flow_control,
						R.layout.my_spinner_textview);
		flowAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		flowSpinner.setAdapter(flowAdapter);
		/* default flow control is is none */
		flowControl = 0;

		portSpinner = (Spinner) findViewById(R.id.portValue);
		portAdapter = ArrayAdapter.createFromResource(usbDeviceContext,
				R.array.port_list_1, R.layout.my_spinner_textview);
		portAdapter.setDropDownViewResource(R.layout.my_spinner_textview);
		portSpinner.setAdapter(portAdapter);
		portNumber = 1;

		/* Set the adapter listeners for baud */
		baudSpinner.setOnItemSelectedListener(new MyOnBaudSelectedListener());
		/* Set the adapter listeners for stop bits */
		stopSpinner.setOnItemSelectedListener(new MyOnStopSelectedListener());
		/* Set the adapter listeners for data bits */
		dataSpinner.setOnItemSelectedListener(new MyOnDataSelectedListener());
		/* Set the adapter listeners for parity */
		paritySpinner
				.setOnItemSelectedListener(new MyOnParitySelectedListener());
		/* Set the adapter listeners for flow control */
		flowSpinner.setOnItemSelectedListener(new MyOnFlowSelectedListener());
		/* Set the adapter listeners for port number */
		portSpinner.setOnItemSelectedListener(new MyOnPortSelectedListener());

		/* Sets onClick Listener for all 4 buttons */
		openButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DevCount <= 0) {
					createDeviceList();
				} else {
					connectFunction();
				}
			}
		});

		configButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DevCount <= 0 || ftDev == null) {
					Toast.makeText(usbDeviceContext, "Device not open yet...",
							Toast.LENGTH_SHORT).show();
				} else {
					SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
				}
			}
		});

		readEnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DevCount <= 0 || ftDev == null) {
					Toast.makeText(usbDeviceContext, "Device not open yet...",
							Toast.LENGTH_SHORT).show();
				} else if (uart_configured == false) {
					Toast.makeText(usbDeviceContext,
							"UART not configure yet...", Toast.LENGTH_SHORT)
							.show();
					return;
				} else {
					EnableRead();
				}
			}
		});

		writeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DevCount <= 0 || ftDev == null) {
					Toast.makeText(usbDeviceContext, "Device not open yet...",
							Toast.LENGTH_SHORT).show();
				} else if (uart_configured == false) {
					Toast.makeText(usbDeviceContext,
							"UART not configure yet...", Toast.LENGTH_SHORT)
							.show();
					return;
				} else {
					SendMessage();
				}
			}
		});

	}

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

		Toast.makeText(usbDeviceContext, "Config done", Toast.LENGTH_SHORT)
				.show();
		uart_configured = true;
	}

	/* Life cycle methods */

	/**
	 * Pobroji spojene uređaje prilikom svakog pokretanja aktivitija i
	 * registriraj broadcast koji prati dal se iskopco usb.
	 */
	@Override
	public void onResume() {
		super.onResume();
		DevCount = 0;
		createDeviceList();

		registerReceiver(mUsbReceiver, new IntentFilter(
				UsbManager.ACTION_USB_DEVICE_DETACHED));
	}

	/**
	 * Zaustavlja pozadinsku dretvu, deregistrira broadcast i odspaja se od
	 * USB-a.
	 */
	@Override
	protected void onStop() {
		if (bReadThreadGoing) {
			bReadThreadGoing = false;
		}
		unregisterReceiver(mUsbReceiver);
		closeUsbDevice();
		super.onStop();
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

	/**
	 * Spaja se na prvi USB uređaj, i ispisuje odgovarajuću poruku u slučaju
	 * neuspjeha.
	 */
	public void connectFunction() {
		int tmpProtNumber = openIndex + 1;

		if (currentIndex != openIndex) {
			if (null == ftDev) {
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, openIndex);
			} else {
				synchronized (ftDev) {
					Toast.makeText(this, "Sxnchronised part entered.",
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
				bReadThreadGoing = true;
				read_thread.start();
			}
		} else {
			Toast.makeText(usbDeviceContext,
					"open device port(" + tmpProtNumber + ") NG",
					Toast.LENGTH_LONG).show();
			// Toast.makeText(DeviceUARTContext, "Need to get permission!",
			// Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Closes opened ftDev.
	 */
	private void closeUsbDevice() {
		DevCount = -1;
		currentIndex = -2;
		bReadThreadGoing = false;

		// glavna dretva mora sacekati pozadinsku dretvu da posalje zadnji
		// objekt handleru, ukoliko se prekine glavna dretva prije no sto je
		// pozadinska poslala poruku glavnoj aplikacija se rusi
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignore) {
		}

		if (ftDev != null) {
			synchronized (ftDev) {
				if (true == ftDev.isOpen()) {
					Toast.makeText(this, "Closing usb", Toast.LENGTH_SHORT)
							.show();
					Log.d(TAG, "Closing usb device connection.");
					ftDev.close();
					ftDev = null;
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

				if (!isRegistered & uart_configured) {
					registerSensor(String.copyValueOf(readDataToText, 0,
							iavailable));
				}
			}
		}
	};

	/**
	 * Dretva koja u pozadini čita podatke sa spojenog usb uređaja i iste
	 * podatke prikazuje u prozoru 'Read Bytes'.
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
					Thread.sleep(1000);
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
						Message msg = mHandler.obtainMessage();
						mHandler.sendMessage(msg);
					}
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
				bReadThreadGoing = false;
				closeUsbDevice();
				onBackPressed();
			}
		}
	};

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Provjerava dal je senzor registriran i registrira ga ukoliko nije.
	 * 
	 * @param sensorName
	 *            Cijeli string koji senzor salje. primjer:
	 *            !moteid!ime!moteid!!b!56!b!!end!
	 */
	private void registerSensor(String sensorName) {

		if ((sensorName.length() > 25) & sensorName.startsWith("!moteid!")) {

			sensorName = sensorName.substring(8);
			sensorName = sensorName.substring(0, sensorName.indexOf('!'));

			WaspmoteApplication waspApp = (WaspmoteApplication) getApplication();
			SensorsDataSource sensors = (SensorsDataSource) waspApp
					.getWaspmoteSqlHelper().getSensorsDataSource(this);
			if (!sensors.SensorExists(sensorName)) {
				sensors.addSensor(sensorName, 3);
				Toast.makeText(this,
						"Sensor: " + sensorName + " is registered!",
						Toast.LENGTH_SHORT).show();
			}
			isRegistered = true;
		}

	}

}
