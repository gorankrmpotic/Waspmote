package hr.fer.zari.waspmote;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.D2xxManager.D2xxException;
import com.ftdi.j2xx.FT_Device;

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

		
	}


	@Override
	protected void onStop() {
		closeUsbDevice();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

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
		if (ftDev != null) {
			if (ftDev.isOpen()) {
				Toast.makeText(this, "Closing usb, error", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Closing usb device connection.");
				ftDev.close();
			}
		}
	}
}
