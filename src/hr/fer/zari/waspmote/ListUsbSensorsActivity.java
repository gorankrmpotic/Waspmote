package hr.fer.zari.waspmote;

import java.util.Arrays;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;

/**
 * Klasa trenutno ima implementiranu funkcionalnost očitavanja svih spojenih Usb
 * uređaja. <b>Trenutno ne prikazuje sve spojene uređaje već samo prvi.</b> Ovo dakako
 * u sljedećim verzijama treba promijeniti.
 * 
 * @author Igor Petkovski
 * @version 1.0
 * 
 */
public class ListUsbSensorsActivity extends ActionBarActivity {

	private Context usbDeviceContext;
	private D2xxManager ftdid2xx;
	private FT_Device ftDev = null;
	private static ListView usbSensorsList;
	private int DevCount = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_usb_sensors);

		// * find views *
		usbSensorsList = (ListView) findViewById(R.id.usb_sensors_list);

		// * initialize usb management objects *
		usbDeviceContext = getApplicationContext();
		try {
			ftdid2xx = D2xxManager.getInstance(usbDeviceContext);
		} catch (D2xxException e) {
			e.printStackTrace();
		}

		// * inflate views *
		inflateDeviceList();

	}

	@Override
	protected void onStop() {
		if (ftDev != null) {
			if (ftDev.isOpen()) {
				Toast.makeText(this, "is open", Toast.LENGTH_SHORT).show();
				ftDev.close();
			}
		}

		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
		DevCount = -1;
		ftDev.close();
		inflateDeviceList();

	}

	public void notifyUSBDeviceAttach() {
		inflateDeviceList();
	}

	public void notifyUSBDeviceDetach() {
		DevCount = -1;
		ftDev = null;
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		onBackPressed();
	}

	/**
	 * Očitava trenutno spojene usb uređaje i popunjava jednostavnu listu istih.
	 * Za uređaje se prikazuje njihov serijski broj. <b>Kako je gore napomenuto,
	 * prikazuje samo prvi spojeni uređaj. </b>
	 */
	private void inflateDeviceList() {
		createDeviceList();
		usbSensorsList = (ListView) findViewById(R.id.usb_sensors_list);
		if (null == ftDev) {
			if (DevCount > 0) {
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, DevCount - 1);
				Toast.makeText(this, "DevCount > 0; c = " + DevCount,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "No devices found!", Toast.LENGTH_SHORT)
						.show();
				onBackPressed();
			}
		} else {
			synchronized (ftDev) {
				Toast.makeText(this, "Was connected previously ",
						Toast.LENGTH_SHORT).show();
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, DevCount - 1);
			}

		}

		if (ftDev != null) {
			Toast.makeText(
					this,
					"Connected Usb device = "
							+ ftDev.getDeviceInfo().serialNumber,
					Toast.LENGTH_SHORT).show();
			usbSensorsList.setAdapter(new ArrayAdapter<>(this,
					android.R.layout.simple_list_item_1, Arrays.asList(ftDev
							.getDeviceInfo().serialNumber)));
		}
	}

	/**
	 * Mjeri broj trenutno spojenih usb uređaja i postavlja privatne varijable
	 * razreda na odgovorajaće vrijednosti.
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

}
