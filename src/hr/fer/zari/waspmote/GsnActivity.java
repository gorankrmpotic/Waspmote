package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.GSNDataSource;
import hr.fer.zari.waspmote.models.GSN;

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
import android.widget.ArrayAdapter;

public class GsnActivity extends Activity {

	WaspmoteApplication waspApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gsn);
		setTitle("GSN");

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		waspApp = (WaspmoteApplication)getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gsn, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_gsn, container,
					false);
			return rootView;
		}
	}
	
	public void NewGsnButtonClicked(View view)
	{
		Intent NewGsnIntent = new Intent(this, NewGsnActivity.class);
		NewGsnIntent.putExtra("Type", "Create");
		startActivity(NewGsnIntent);
	}
	
	public void DeleteGsnButtonClicked(View view)
	{
		final AlertDialog.Builder gsnDialog = new AlertDialog.Builder(GsnActivity.this);
		gsnDialog.setTitle("GSNs");
		gsnDialog.setNegativeButton("Cancel", null);
		final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		final GSNDataSource gsnData = (GSNDataSource)waspApp.getWaspmoteSqlHelper().getGSNDataSource(this);
		List<GSN> gsns = gsnData.getAllGSN();
		for(GSN gsn : gsns)
		{
			mAdapter.add(gsn.getGSNName()+":"+gsn.getIp());
		}
		gsnDialog.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String[] gsnSplit = mAdapter.getItem(which).split(":");
				final String gsnName = gsnSplit[0];
				AlertDialog.Builder confirm = new AlertDialog.Builder(GsnActivity.this);
				confirm.setTitle("Warning");
				confirm.setMessage("Are you sure you want to delete this GSN?");
				confirm.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						gsnData.deleteGSN(gsnData.getGSNByName(gsnName));
					}
				});
				confirm.setNegativeButton("Cancel", null);
				confirm.show();
			}
		});
		AlertDialog alertDialogGsn = gsnDialog.create();
		alertDialogGsn.show();
		
	}
	
	public void EditGsnButtonClicked(View view)
	{
		final AlertDialog.Builder gsnDialog = new AlertDialog.Builder(GsnActivity.this);
		gsnDialog.setTitle("GSNs");
		gsnDialog.setNegativeButton("Cancel", null);
		final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		final GSNDataSource gsnData = (GSNDataSource)waspApp.getWaspmoteSqlHelper().getGSNDataSource(this);
		List<GSN> gsns = gsnData.getAllGSN();
		for(GSN gsn : gsns)
		{
			mAdapter.add(gsn.getGSNName()+":"+gsn.getIp());
		}
		gsnDialog.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String[] gsnSplit = mAdapter.getItem(which).split(":");
				String gsnName = gsnSplit[0];
				OpenEditGsnActivity(gsnName);
			}
		});
		
		AlertDialog alertDialogGSN = gsnDialog.create();
		alertDialogGSN.show();
	}
	
	public void OpenEditGsnActivity(String gsnName)
	{
		Intent EditGsnIntent = new Intent(this, NewGsnActivity.class);
		EditGsnIntent.putExtra("Type", "Edit");
		EditGsnIntent.putExtra("GSNName", gsnName);
		startActivity(EditGsnIntent);
	}

}
