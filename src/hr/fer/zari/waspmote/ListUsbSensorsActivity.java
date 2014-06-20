package hr.fer.zari.waspmote;

import java.util.Arrays;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;

/**
 * Klasa trenutno ima implementiranu funkcionalnost očitavanja svih spojenih Usb
 * uređaja. <b>Trenutno ne prikazuje sve spojene uređaje već samo prvi.</b> Ovo
 * dakako u sljedećim verzijama treba promijeniti.
 * 
 * @author Igor Petkovski
 * @version 1.0
 * 
 */
public class ListUsbSensorsActivity extends ActionBarActivity {

	private static final String TAG = ListUsbSensorsActivity.class
			.getSimpleName();
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
		closeUsbDevice();
		//Toast.makeText(this, "on stop", Toast.LENGTH_SHORT).show();
		super.onStop();
	}

	@Override
	protected void onPause() {
		closeUsbDevice();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
		inflateDeviceList();
	}

	public void notifyUSBDeviceAttach() {
		inflateDeviceList();
	}

	public void notifyUSBDeviceDetach() {
		closeUsbDevice();
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
				//Toast.makeText(this, "DevCount = " + DevCount, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "No devices found!", Toast.LENGTH_SHORT)
						.show();
				onBackPressed();
			}
		} else {
			synchronized (ftDev) {
				ftDev = ftdid2xx.openByIndex(usbDeviceContext, DevCount - 1);
				// Toast.makeText(this, "Was connected previously.", Toast.LENGTH_SHORT).show();
			}
		}

		if (ftDev != null) {
			usbSensorsList.setAdapter(new ArrayAdapter<>(this,
					android.R.layout.simple_list_item_1, Arrays.asList(ftDev
							.getDeviceInfo().serialNumber)));
			
			usbSensorsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		        @Override
				public void onItemClick(AdapterView<?> av, View view, int i, long l) {
		            //Toast.makeText(ListUsbSensorsActivity.this, "Pos. clocked: "+ i, Toast.LENGTH_LONG).show();
		            Intent intent = new Intent(av.getContext(), ViewUsbSensorDataActivity.class);
		            intent.putExtra("ClickedUsbIndex", i);
		            closeUsbDevice();
		            startActivity(intent);
		        }
		    });
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

	/**
	 * Closes opened ftDev.
	 */
	private void closeUsbDevice() {
		DevCount = -1;
		if(ftDev != null)
		{
			synchronized(ftDev)
			{
				if(true == ftDev.isOpen())
				{
					//Toast.makeText(this, "Closing usb in listed", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Closing usb device connection.");
					ftDev.close();
				}
			}
		}
	}

}
