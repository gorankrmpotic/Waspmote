package hr.fer.zari.waspmote;



import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SubscriptionDataSource;
import hr.fer.zari.waspmote.models.SensorMeasurement;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity{
	
	WaspmoteApplication waspApp;
	SubscriptionDataSource subscriptionData;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle(getString(R.string.app_name));

				
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		waspApp = (WaspmoteApplication)getApplication();
		subscriptionData = (SubscriptionDataSource) waspApp.getWaspmoteSqlHelper().getSubscriptionDataSource(this);
		
		//provjera dal je user guest!
		if(waspApp.getCurrentUser() == null)
		{
			Button adminButton = (Button) findViewById(R.id.AdministrationButton);
			adminButton.setVisibility(View.GONE);
			Button gsnButton = (Button)findViewById(R.id.GSNButton);
			gsnButton.setVisibility(View.GONE);
			Button subscriptionButton = (Button) findViewById(R.id.SubscriptionButton);
			subscriptionButton.setVisibility(View.GONE);
			Button deleteButton = (Button) findViewById(R.id.DeleteDataButton);
			deleteButton.setVisibility(View.GONE);
		}
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	public void InternalSensorsButtonClicked(View view)
	{
		Intent InternalSensorsIntent = new Intent(this, InternalSensorsActivity.class);
		startActivity(InternalSensorsIntent);
	}
	
	public void AdministrationButtonClicked(View view)
	{
		Intent AdministrationIntent = new Intent(this, AdministrationActivity.class);
		startActivity(AdministrationIntent);
	}
	
	public void LogOutButtonClicked(View view)
	{
		//odjavi usera!!!
		waspApp.logOut();
		finish();
	}
	
	public void ExternalSensorButtonClicked(View view)
	{
		Intent ExternalSensorIntent = new Intent(this, ExternalSensorActivity.class);
		startActivity(ExternalSensorIntent);
	}
	
	public void UsbSensorButtonClicked(View view)
	{
		Intent ExternalSensorIntent = new Intent(this, ListUsbSensorsActivity.class);
		startActivity(ExternalSensorIntent);
	}
	
	public void PlotSensorData(View view)
	{
		Intent PlotActivityIntent = new Intent(this, PlotActivity.class);
		startActivity(PlotActivityIntent);		
	}
	
	public void GSNButtonClicked(View view)
	{
		Intent GsnIntent = new Intent(this, GsnActivity.class);
		startActivity(GsnIntent);
	}
	
	public void SubscriptionButtonClicked(View view)
	{
		Intent SubscriptionIntent = new Intent(this, SubscriptionActivity.class);
		startActivity(SubscriptionIntent);
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub		
		AlertDialog.Builder backWarning = new AlertDialog.Builder(MainActivity.this);
		backWarning.setTitle("Warning");
		backWarning.setMessage("Are you sure you want to log out?");
		backWarning.setNegativeButton("No", null);
		backWarning.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				waspApp.logOut();
				goBack();
			}
		});
		backWarning.show();
		
	}
	
	public void goBack()
	{
		super.onBackPressed();
	}


	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		if(subscriptionData.getAllSubscriptions().size() > 0)
		{
			Button externalSensButton = (Button) findViewById(R.id.ExternalSensorButton);
			externalSensButton.setVisibility(View.GONE);
		}
		else
		{
			Button externalSensButton = (Button) findViewById(R.id.ExternalSensorButton);
			externalSensButton.setVisibility(View.VISIBLE);			
		}
		super.onPostResume();
	}
	
	public void DeleteDataButtonClicked(View view)
	{
		final SensorMeasurementDataSource s = (SensorMeasurementDataSource)waspApp.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);
		List<SensorMeasurement> listSensMes = s.getAllSensorMeasurement();
		if(listSensMes.size() == 0)
		{
			AlertDialog.Builder emptyTable = new AlertDialog.Builder(MainActivity.this);
			emptyTable.setTitle("Delete Data");
			emptyTable.setMessage("There is no data to delete!");
			emptyTable.setPositiveButton("Ok", null);
			emptyTable.show();
		}
		else
		{
			AlertDialog.Builder dataInTable = new AlertDialog.Builder(MainActivity.this);
			dataInTable.setTitle("Warning");
			dataInTable.setMessage("Are you sure you want to delete data from database?");
			dataInTable.setNegativeButton("No", null);
			dataInTable.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					s.DeleteAllSensorMeasurements();
				}
			});
			dataInTable.show();
		}
		
	}



}
