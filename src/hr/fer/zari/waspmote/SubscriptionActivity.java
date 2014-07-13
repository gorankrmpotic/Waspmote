package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.SubscriptionDataSource;
import hr.fer.zari.waspmote.models.Subscription;
import hr.fer.zari.waspmote.services.GsnService;
import hr.fer.zari.waspmote.services.SensorMeasurementService;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SubscriptionActivity extends Activity {

	WaspmoteApplication waspApp;
	SubscriptionDataSource subscriptionData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscription);
		setTitle("Subscription");
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		waspApp = (WaspmoteApplication)getApplication();
		subscriptionData = (SubscriptionDataSource) waspApp.getWaspmoteSqlHelper().getSubscriptionDataSource(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.subscription, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_subscription,
					container, false);
			return rootView;
		}
	}
	
	public void CreateSubscriptionButtonClicked(View view)
	{
		Intent createSubscriptionIntent = new Intent(this, NewSubscriptionActivity.class);
		createSubscriptionIntent.putExtra("Type", "Create");
		startActivity(createSubscriptionIntent);
	}
	
	public void DeleteSubscriptionButtonClicked(View view)
	{		
		//samo brisi
		//pretpostavljam da je tu samo 1 subscription!
		List<Subscription> subscriptions = subscriptionData.getAllSubscriptions();
		if(subscriptions.isEmpty())
		{
			AlertDialog.Builder noSubscription = new AlertDialog.Builder(SubscriptionActivity.this);
			noSubscription.setTitle("Error");
			noSubscription.setMessage("Subscription doesn't exist");
			noSubscription.setPositiveButton("Ok", null);
			noSubscription.show();
		}
		else
		{
			final Subscription subscriptionToDelete = subscriptions.get(0);
			AlertDialog.Builder confirmSubscriptionDeletion = new AlertDialog.Builder(SubscriptionActivity.this);
			confirmSubscriptionDeletion.setTitle("Warning");
			confirmSubscriptionDeletion.setMessage("Are you sure you want to delete this subscription!");
			confirmSubscriptionDeletion.setNegativeButton("Cancel", null);
			confirmSubscriptionDeletion.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					subscriptionData.deleteSubscription(subscriptionToDelete);	
					stopService(new Intent(getBaseContext(), SensorMeasurementService.class));
					stopService(new Intent(getBaseContext(), GsnService.class));
				}
			});
			confirmSubscriptionDeletion.show();
		}
	}
	
	public void EditSubscriptionButtonClicked(View view)
	{
		List<Subscription> subscriptions = subscriptionData.getAllSubscriptions();
		if(subscriptions.isEmpty())
		{
			AlertDialog.Builder noSubscription = new AlertDialog.Builder(SubscriptionActivity.this);
			noSubscription.setTitle("Error");
			noSubscription.setMessage("Subscription doesn't exist");
			noSubscription.setPositiveButton("Ok", null);
			noSubscription.show();
		}
		else
		{
			Intent editSubscriptionIntent = new Intent(this, NewSubscriptionActivity.class);
			editSubscriptionIntent.putExtra("Type", "Edit");
			startActivity(editSubscriptionIntent);
		}
	}

}
