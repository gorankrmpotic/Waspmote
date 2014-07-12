package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.GSNDataSource;
import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SensorSubscriptionDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.db.dao.SubscriptionDataSource;
import hr.fer.zari.waspmote.models.GSN;
import hr.fer.zari.waspmote.models.SensorSubscription;
import hr.fer.zari.waspmote.models.Sensors;
import hr.fer.zari.waspmote.models.Subscription;
import hr.fer.zari.waspmote.services.GsnService;
import hr.fer.zari.waspmote.services.SensorMeasurementService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class NewSubscriptionActivity extends Activity {

	String ActivityType;
	WaspmoteApplication waspApp;
	GSNDataSource gsnData;
	MyCustomAdapter dataAdapter = null;
	SensorsDataSource sensorsData;
	SubscriptionDataSource subscriptionData;
	SensorSubscriptionDataSource sensorSubscriptionData;
	ArrayList<ListViewSensorItems> listItems;	
	SensorMeasurementDataSource sensorMeasurementData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_subscription);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		waspApp = (WaspmoteApplication)getApplication();
		gsnData = (GSNDataSource)waspApp.getWaspmoteSqlHelper().getGSNDataSource(this);
		sensorsData = (SensorsDataSource) waspApp.getWaspmoteSqlHelper().getSensorsDataSource(this);
		subscriptionData = (SubscriptionDataSource) waspApp.getWaspmoteSqlHelper().getSubscriptionDataSource(this);
		sensorSubscriptionData = (SensorSubscriptionDataSource) waspApp.getWaspmoteSqlHelper().getSensorSubscriptionDataSource(this);
		sensorMeasurementData = (SensorMeasurementDataSource) waspApp.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);
		Bundle bundle = getIntent().getExtras();
		ActivityType = bundle.getString("Type");		
		
		Button activityButton = (Button)findViewById(R.id.NewSubscriptionButton);
		switch(ActivityType)
		{
		case "Create":
			setTitle("Create Subscription");
			activityButton.setText("Create");
			FillSpinner();
			FillListView();
			break;
		case "Edit":
			setTitle("Edit Subscription");
			activityButton.setText("Change");
			//FillListView koji prima listu selektiranih!
			//dohvati selektirane senzore
			List<SensorSubscription> sensSub = sensorSubscriptionData.getAllSensorSubscriptions();
			List<String> selectedSensorNames = new ArrayList<String>();
			for(SensorSubscription sub : sensSub)
			{
				Sensors s = sensorsData.getSensorById(sub.get_idSensor());
				selectedSensorNames.add(s.getSensorName());				
			}
			FillListViewEdit(selectedSensorNames);
			EditText period = (EditText) findViewById(R.id.periodEditText);			
			Subscription oldSub = subscriptionData.getAllSubscriptions().get(0);
			period.setText(String.valueOf(oldSub.getPeriod()));
			FillSpinnerEdit(oldSub.get_idGSN());
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_subscription, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_new_subscription, container, false);
			return rootView;
		}
	}
	
	public void FillSpinner()
	{
		Spinner s = (Spinner)findViewById(R.id.GsnSpinner);
		List<GSN> gsns = gsnData.getAllGSN();		
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
		
		for(GSN gsn : gsns)
		{			
			mAdapter.add(gsn.getGSNName()+":"+gsn.getIp());
		}
		s.setAdapter(mAdapter);
	}
	
	public void FillSpinnerEdit(int selectedGsnId)
	{
		Spinner s = (Spinner)findViewById(R.id.GsnSpinner);
		
		List<GSN> gsns = gsnData.getAllGSN();		
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
		int position = 0;
		int cnt = 0;
		for(GSN gsn : gsns)
		{			
			if(gsn.get_id() == selectedGsnId)
			{
				position = cnt;
			}
			cnt++;
			mAdapter.add(gsn.getGSNName()+":"+gsn.getIp());
		}
		s.setAdapter(mAdapter);
		s.setSelection(position);
	}
	
	public void CancelSubscriptionButtonClicked(View view)
	{
		finish();
	}
	
	public void NewSubscriptionButtonClicked(View view)
	{
		//obrisi subscription iz baze -> ako postoji
		final List<Subscription> subs = subscriptionData.getAllSubscriptions();		
		if(!subs.isEmpty() && ActivityType.trim().equals("Create"))		{
			
			//za obrnutu situaciju !subs.isEmpty() daj upozorenje da ce obrisati postojeci!!
			AlertDialog.Builder confirmDialog = new AlertDialog.Builder(NewSubscriptionActivity.this);
			confirmDialog.setTitle("Warning");
			confirmDialog.setMessage("Creating this subscription will delete the previous one!");
			confirmDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					subscriptionData.deleteSubscription(subs.get(0));
					//brisem stari servis i radim novi!
					stopService(new Intent(getBaseContext(), SensorMeasurementService.class));
					List<String> selectedSensors = new ArrayList<String>();
					for(ListViewSensorItems item : listItems)
					{		
						if(item.isSelected())
						{
							selectedSensors.add(item.getName());								
						}
					}		
					Spinner gsnSpin = (Spinner) findViewById(R.id.GsnSpinner);
					EditText periodInput = (EditText) findViewById(R.id.periodEditText);		
					if(gsnSpin.getCount() == 0)
					{
						AlertDialog.Builder noGsn = new AlertDialog.Builder(NewSubscriptionActivity.this);
						noGsn.setTitle("Error");
						noGsn.setMessage("You didn't select GSN!");
						noGsn.setPositiveButton("Ok", null);
						noGsn.show();
					}		
					else if(periodInput.getText().toString().isEmpty())
					{
						AlertDialog.Builder noPeriod = new AlertDialog.Builder(NewSubscriptionActivity.this);
						noPeriod.setTitle("Error");
						noPeriod.setMessage("Period can't be empty!");
						noPeriod.setPositiveButton("Ok", null);
						noPeriod.show();
					}
					else if(selectedSensors.isEmpty())
					{
						AlertDialog.Builder noSensorsSelected = new AlertDialog.Builder(NewSubscriptionActivity.this);
						noSensorsSelected.setTitle("Error");
						noSensorsSelected.setMessage("You need to select sensors!");
						noSensorsSelected.setPositiveButton("Ok", null);
						noSensorsSelected.show();
					}
					else
					{
						//dohvati idGSNa
						String[] gsnSpinSplit = gsnSpin.getSelectedItem().toString().split(":");
						String gsnName = gsnSpinSplit[0];
						final GSN selectedGsn = gsnData.getGSNByName(gsnName);			
						//napravi subscription						
						subscriptionData.addSubscription(selectedGsn.get_id(), Integer.parseInt(periodInput.getText().toString()));
						//dohvati id subscriptiona
						List<Subscription> subsForId = subscriptionData.getAllSubscriptions();
						int idSubs = subsForId.get(subsForId.size()-1).get_id();
						
						// **** dohvati idjeve senzora ****
						final List<Sensors> sensorsForService = new ArrayList<Sensors>();
						for(String sensorName : selectedSensors)
						{							
							Sensors selectedSens = sensorsData.getSensorByName(sensorName);
							sensorsForService.add(selectedSens);
							sensorSubscriptionData.addSensorSubscription(idSubs, selectedSens.get_id());
						}
						final int period = Integer.parseInt(periodInput.getText().toString());
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								Intent subsService = new Intent(getBaseContext(), SensorMeasurementService.class);						
								//ServiceData sd = new ServiceData(sensorsForService, period);
								ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
								Bundle bundle = new Bundle();
								bundle.putSerializable("ServiceData", sd);						
								subsService.putExtras(bundle);				
								getBaseContext().startService(subsService);
							}
						}).start();	
//						new Thread(new Runnable() {							
//							@Override
//							public void run() {
//								Intent subsService = new Intent(getBaseContext(), GsnService.class);						
//								//ServiceData sd = new ServiceData(sensorsForService, period);
//								ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
//								Bundle bundle = new Bundle();
//								bundle.putSerializable("ServiceData", sd);						
//								subsService.putExtras(bundle);				
//								getBaseContext().startService(subsService);
//							}
//						}).start();	
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {
							
							@Override
							public void run() {
								new Thread(new Runnable() {							
									@Override
									public void run() {
										Intent subsService = new Intent(getBaseContext(), GsnService.class);						
										//ServiceData sd = new ServiceData(sensorsForService, period);
										ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
										Bundle bundle = new Bundle();
										bundle.putSerializable("ServiceData", sd);						
										subsService.putExtras(bundle);				
										getBaseContext().startService(subsService);
									}
								}).start();	
							}
						}, 1000);
						finish();
					}			
				}
			});
			confirmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			confirmDialog.show();
		}
		
		//dohvati sve elemente osim listviewa koji puni podatke u klasu globalno dostupnu
		else if(ActivityType.equals("Edit"))
		{
		List<String> selectedSensors = new ArrayList<String>();
		for(ListViewSensorItems item : listItems)
		{		
			if(item.isSelected())
			{
				selectedSensors.add(item.getName());								
			}
		}		
		Spinner gsnSpin = (Spinner) findViewById(R.id.GsnSpinner);
		EditText periodInput = (EditText) findViewById(R.id.periodEditText);		
		if(gsnSpin.getCount() == 0)
		{
			AlertDialog.Builder noGsn = new AlertDialog.Builder(NewSubscriptionActivity.this);
			noGsn.setTitle("Error");
			noGsn.setMessage("You didn't select GSN!");
			noGsn.setPositiveButton("Ok", null);
			noGsn.show();
		}		
		else if(periodInput.getText().toString().isEmpty())
		{
			AlertDialog.Builder noPeriod = new AlertDialog.Builder(NewSubscriptionActivity.this);
			noPeriod.setTitle("Error");
			noPeriod.setMessage("Period can't be empty!");
			noPeriod.setPositiveButton("Ok", null);
			noPeriod.show();
		}
		else if(selectedSensors.isEmpty())
		{
			AlertDialog.Builder noSensorsSelected = new AlertDialog.Builder(NewSubscriptionActivity.this);
			noSensorsSelected.setTitle("Error");
			noSensorsSelected.setMessage("You need to select sensors!");
			noSensorsSelected.setPositiveButton("Ok", null);
			noSensorsSelected.show();
		}
		else
		{
			//dohvati idGSNa
			String[] gsnSpinSplit = gsnSpin.getSelectedItem().toString().split(":");
			String gsnName = gsnSpinSplit[0];
			final GSN selectedGsn = gsnData.getGSNByName(gsnName);			
			//napravi subscription
			if(ActivityType.equals("Edit"))
			{
				subscriptionData.deleteSubscription(subs.get(0));
			}
			subscriptionData.addSubscription(selectedGsn.get_id(), Integer.parseInt(periodInput.getText().toString()));
			//dohvati id subscriptiona
			List<Subscription> subsForId = subscriptionData.getAllSubscriptions();
			int idSubs = subsForId.get(subsForId.size()-1).get_id();
			final List<Sensors> sensorsForService = new ArrayList<Sensors>();
			//dohvati idjeve senzora
			for(String sensorName : selectedSensors)
			{
				Sensors selectedSens = sensorsData.getSensorByName(sensorName);
				sensorsForService.add(selectedSens);
				sensorSubscriptionData.addSensorSubscription(idSubs, selectedSens.get_id());
			}
			final int period = Integer.parseInt(periodInput.getText().toString());
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Intent subsService = new Intent(getBaseContext(), SensorMeasurementService.class);						
					//ServiceData sd = new ServiceData(sensorsForService, period);
					ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
					Bundle bundle = new Bundle();
					bundle.putSerializable("ServiceData", sd);						
					subsService.putExtras(bundle);				
					getBaseContext().startService(subsService);
				}
			}).start();	
			
//			new Thread(new Runnable() {							
//				@Override
//				public void run() {
//					Intent subsService = new Intent(getBaseContext(), GsnService.class);						
//					//ServiceData sd = new ServiceData(sensorsForService, period);
//					ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
//					Bundle bundle = new Bundle();
//					bundle.putSerializable("ServiceData", sd);						
//					subsService.putExtras(bundle);				
//					getBaseContext().startService(subsService);
//				}
//			}).start();	
			
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					new Thread(new Runnable() {							
						@Override
						public void run() {
							Intent subsService = new Intent(getBaseContext(), GsnService.class);						
							//ServiceData sd = new ServiceData(sensorsForService, period);
							ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
							Bundle bundle = new Bundle();
							bundle.putSerializable("ServiceData", sd);						
							subsService.putExtras(bundle);				
							getBaseContext().startService(subsService);
						}
					}).start();	
				}
			}, 1000);
			finish();
		}	
		}
		else
		{
			List<String> selectedSensors = new ArrayList<String>();
			for(ListViewSensorItems item : listItems)
			{		
				if(item.isSelected())
				{
					selectedSensors.add(item.getName());								
				}
			}		
			Spinner gsnSpin = (Spinner) findViewById(R.id.GsnSpinner);
			EditText periodInput = (EditText) findViewById(R.id.periodEditText);		
			if(gsnSpin.getCount() == 0)
			{
				AlertDialog.Builder noGsn = new AlertDialog.Builder(NewSubscriptionActivity.this);
				noGsn.setTitle("Error");
				noGsn.setMessage("You didn't select GSN!");
				noGsn.setPositiveButton("Ok", null);
				noGsn.show();
			}		
			else if(periodInput.getText().toString().isEmpty())
			{
				AlertDialog.Builder noPeriod = new AlertDialog.Builder(NewSubscriptionActivity.this);
				noPeriod.setTitle("Error");
				noPeriod.setMessage("Period can't be empty!");
				noPeriod.setPositiveButton("Ok", null);
				noPeriod.show();
			}
			else if(selectedSensors.isEmpty())
			{
				AlertDialog.Builder noSensorsSelected = new AlertDialog.Builder(NewSubscriptionActivity.this);
				noSensorsSelected.setTitle("Error");
				noSensorsSelected.setMessage("You need to select sensors!");
				noSensorsSelected.setPositiveButton("Ok", null);
				noSensorsSelected.show();
			}
			else
			{
				//dohvati idGSNa
				String[] gsnSpinSplit = gsnSpin.getSelectedItem().toString().split(":");
				String gsnName = gsnSpinSplit[0];
				final GSN selectedGsn = gsnData.getGSNByName(gsnName);			
				//napravi subscription						
				subscriptionData.addSubscription(selectedGsn.get_id(), Integer.parseInt(periodInput.getText().toString()));
				//dohvati id subscriptiona
				List<Subscription> subsForId = subscriptionData.getAllSubscriptions();
				int idSubs = subsForId.get(subsForId.size()-1).get_id();
				//dohvati idjeve senzora
				final List<Sensors> sensorsForService = new ArrayList<Sensors>();
				for(String sensorName : selectedSensors)
				{
					Sensors selectedSens = sensorsData.getSensorByName(sensorName);
					sensorsForService.add(selectedSens);
					sensorSubscriptionData.addSensorSubscription(idSubs, selectedSens.get_id());
				}
				final int period = Integer.parseInt(periodInput.getText().toString());
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						Intent subsService = new Intent(getBaseContext(), SensorMeasurementService.class);						
						//ServiceData sd = new ServiceData(sensorsForService, period);
						ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
						Bundle bundle = new Bundle();
						bundle.putSerializable("ServiceData", sd);						
						subsService.putExtras(bundle);				
						getBaseContext().startService(subsService);
					}
				}).start();	
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						new Thread(new Runnable() {							
							@Override
							public void run() {
								Intent subsService = new Intent(getBaseContext(), GsnService.class);						
								//ServiceData sd = new ServiceData(sensorsForService, period);
								ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
								Bundle bundle = new Bundle();
								bundle.putSerializable("ServiceData", sd);						
								subsService.putExtras(bundle);				
								getBaseContext().startService(subsService);
							}
						}).start();	
					}
				}, 1000);
//				new Thread(new Runnable() {							
//					@Override
//					public void run() {
//						Intent subsService = new Intent(getBaseContext(), GsnService.class);						
//						//ServiceData sd = new ServiceData(sensorsForService, period);
//						ServiceData sd = new ServiceData(sensorsForService, period, selectedGsn.getIp(), selectedGsn.getGSNUsername(), selectedGsn.getGSNPassword());
//						Bundle bundle = new Bundle();
//						bundle.putSerializable("ServiceData", sd);						
//						subsService.putExtras(bundle);				
//						getBaseContext().startService(subsService);
//					}
//				}).start();	
				
				finish();
			}
		}
		
		//napravi novi subs u bazu
		//pokreni novi servis!!
		//ako postoji servis, obrisi ga, 
	}
	
	public void FillListView()
	{
//		ArrayList<ListViewSensorItems> listItems = new ArrayList<ListViewSensorItems>();
		listItems = new ArrayList<ListViewSensorItems>();
		List<Sensors> sens = sensorsData.getAllSensors();
		for(Sensors s : sens)
		{
			listItems.add(new ListViewSensorItems(s.getSensorName(), false));
		}
		dataAdapter = new MyCustomAdapter(this, R.layout.list_view_check_box, listItems);
		ListView lw = (ListView) findViewById(R.id.SubscriptionSensorsListView);
		lw.setAdapter(dataAdapter);
	}
	
	public void FillListViewEdit(List<String> selectedSensors)
	{
		listItems = new ArrayList<ListViewSensorItems>();
		List<Sensors> sens = sensorsData.getAllSensors();
		for(Sensors s : sens)
		{
			if(selectedSensors.contains(s.getSensorName()))
			{
				listItems.add(new ListViewSensorItems(s.getSensorName(), true));
			}
			else
			{
				listItems.add(new ListViewSensorItems(s.getSensorName(), false));				
			}
		}
		dataAdapter = new MyCustomAdapter(this, R.layout.list_view_check_box, listItems);
		ListView lw = (ListView) findViewById(R.id.SubscriptionSensorsListView);
		lw.setAdapter(dataAdapter);
	}
	
	private class MyCustomAdapter extends ArrayAdapter<ListViewSensorItems>
	{
		private ArrayList<ListViewSensorItems> listSensorItems;
		
		public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<ListViewSensorItems> listSensorItems)
		{
			super(context, textViewResourceId, listSensorItems);
			this.listSensorItems = new ArrayList<ListViewSensorItems>();
			this.listSensorItems.addAll(listSensorItems);			
		}
		
		private class ViewHolder {			
			CheckBox name;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			if (convertView == null) 
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_view_check_box, null);
				
				holder = new ViewHolder();
				holder.name = (CheckBox)convertView.findViewById(R.id.checkBoxSensors);
				
				convertView.setTag(holder);
				
				holder.name.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						ListViewSensorItems _item = (ListViewSensorItems) cb.getTag();
						Toast.makeText(getApplicationContext(), "Sensor: "+cb.getText()+" -> "+cb.isChecked()  , Toast.LENGTH_LONG).show();
						_item.setSelected(cb.isChecked());
					}
				});
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();				
			}
			
			ListViewSensorItems item = listSensorItems.get(position);
			holder.name.setText(item.getName());
			holder.name.setChecked(item.isSelected());
			holder.name.setTag(item);
			
			return convertView;			
		}
	}

}
