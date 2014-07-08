package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.GSNDataSource;
import hr.fer.zari.waspmote.models.GSN;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class NewGsnActivity extends Activity {

	String ActivityType;	
	WaspmoteApplication waspApp;
	GSNDataSource gsnData;
	String editGsn = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_gsn);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		waspApp = (WaspmoteApplication)getApplication();
		gsnData = (GSNDataSource)waspApp.getWaspmoteSqlHelper().getGSNDataSource(this);
		Bundle bundle = getIntent().getExtras();
		ActivityType = bundle.getString("Type");
		Button createButton = (Button)findViewById(R.id.GsnCreateButton);
		
		switch(ActivityType)
		{
		case "Create":
			setTitle("Create GSN");
			createButton.setText("Create");
			break;
		case "Edit":
			setTitle("Edit GSN");
			editGsn = bundle.getString("GSNName");
			
			EditText name = (EditText)findViewById(R.id.GsnNameEditText);
			EditText ip = (EditText)findViewById(R.id.GsnIpEditText);
			EditText username = (EditText)findViewById(R.id.GsnUsernameEditText);
			EditText password = (EditText)findViewById(R.id.GsnPasswordEditText);
			
			GSN gsnOldData = gsnData.getGSNByName(editGsn);
			name.setText(gsnOldData.getGSNName());
			ip.setText(gsnOldData.getIp());
			username.setText(gsnOldData.getGSNUsername());
			password.setText(gsnOldData.getGSNPassword());
			
			createButton.setText("Change");
			break;
		default:
			break;
		
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_gsn, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_new_gsn,
					container, false);
			return rootView;
		}
	}
	
	public void GsnCancelButtonClicked(View view)
	{
		finish();
	}
	
	public void GsnCreateButtonClicked(View view)
	{
		EditText gsnNameInput = (EditText)findViewById(R.id.GsnNameEditText);
		EditText gsnIpInput = (EditText)findViewById(R.id.GsnIpEditText);
		EditText gsnUsernameInput = (EditText)findViewById(R.id.GsnUsernameEditText);
		EditText gsnPasswordInput = (EditText)findViewById(R.id.GsnPasswordEditText);
		
		if(gsnNameInput.getText().toString().isEmpty() || gsnIpInput.getText().toString().isEmpty())
		{
			AlertDialog.Builder emptyField = new AlertDialog.Builder(NewGsnActivity.this);
			emptyField.setTitle("Error");
			emptyField.setMessage("You need to fill all fields that are not optional!!");
			emptyField.setPositiveButton("Ok", null);
			emptyField.show();
		}
		else if(!gsnIpInput.getText().toString().contains(":"))
		{
			AlertDialog.Builder invalidPort = new AlertDialog.Builder(NewGsnActivity.this);
			invalidPort.setTitle("Error");
			invalidPort.setMessage("Ip address needs to have port divided by ':'");
			invalidPort.setPositiveButton("Ok", null);
			invalidPort.show();
		}
		else if(!editGsn.isEmpty())
		{
			//edit
			if(gsnData.GSNExists(gsnNameInput.getText().toString()) && !gsnNameInput.getText().toString().equals(editGsn))
			{
				AlertDialog.Builder gsnExists = new AlertDialog.Builder(NewGsnActivity.this);
				gsnExists.setTitle("Error");
				gsnExists.setMessage("GSN with that name already exists!");
				gsnExists.setPositiveButton("Ok", null);
				gsnExists.show();
			}
			else
			{
				gsnData.updateGSN(gsnIpInput.getText().toString(), gsnNameInput.getText().toString(), gsnUsernameInput.getText().toString(), gsnPasswordInput.getText().toString(), editGsn);
				finish();
			}
		}
		else
		{
			//add new
			if(gsnData.GSNExists(gsnNameInput.getText().toString()))
			{
				AlertDialog.Builder gsnExists = new AlertDialog.Builder(NewGsnActivity.this);
				gsnExists.setTitle("Error");
				gsnExists.setMessage("GSN with that name already exists!");
				gsnExists.setPositiveButton("Ok", null);
				gsnExists.show();
			}
			else
			{
				gsnData.addGSN(gsnIpInput.getText().toString(), gsnNameInput.getText().toString(), gsnUsernameInput.getText().toString(), gsnPasswordInput.getText().toString());
				finish();
			}
		}
		
		
	}


}
