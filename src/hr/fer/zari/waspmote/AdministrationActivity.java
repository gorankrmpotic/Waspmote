package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.UserDataSource;
import hr.fer.zari.waspmote.models.User;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class AdministrationActivity extends ActionBarActivity {

	WaspmoteApplication waspApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_administration);
		setTitle("Administration");
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		waspApp = (WaspmoteApplication)getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.administration, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_administration,
					container, false);
			return rootView;
		}
	}

	public void CreateUserButtonClicked(View view) {
		Intent CreateUserIntent = new Intent(this, RegisterActivity.class);
		CreateUserIntent.putExtra("Type", "Create");
		startActivity(CreateUserIntent);
	}

	public void EditUserButtonClicked(View view) {
		// ovo će se izvoditi u međukoraku u kojem će se birat user...zasad nek
		// bude
		// potrebno ispuniti polja koriste�i podatke odabranog usera!!
		
		final AlertDialog.Builder userDialog = new AlertDialog.Builder(AdministrationActivity.this);
		userDialog.setTitle("Users");
		userDialog.setNegativeButton("Cancel", null);
		final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		final UserDataSource userData =(UserDataSource) waspApp.getWaspmoteSqlHelper().getUserDataSource(this);
		List<User> users = userData.getAllUsers();
		for(User user : users)
		{			
			mAdapter.add(user.getUsername()+":"+" "+user.getFirstName()+" "+user.getLastName());
		}
		userDialog.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//dohvati username -> splitaj po : i uzmi 0-ti element
				String[] usernameSplit = mAdapter.getItem(which).split(":");
				final String username = usernameSplit[0];	
				OpenEditActivity(username);
			}
		});
		AlertDialog alertDialogUsers = userDialog.create();
		alertDialogUsers.show();
		
//		Intent EditUserIntent = new Intent(this, RegisterActivity.class);
//		EditUserIntent.putExtra("Type", "Edit");
//		startActivity(EditUserIntent);
	}
	
	public void OpenEditActivity(String username)
	{
		Intent EditUserIntent = new Intent(this, RegisterActivity.class);
		EditUserIntent.putExtra("Type", "Edit");
		EditUserIntent.putExtra("Username", username);
		startActivity(EditUserIntent);
	}
	
	public void DeleteUserButtonClicked(View view)
	{
		//izlistaj sve usere!
		final AlertDialog.Builder userDialog = new AlertDialog.Builder(AdministrationActivity.this);
		userDialog.setTitle("Users");
		userDialog.setNegativeButton("Cancel", null);

		final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		final UserDataSource userData =(UserDataSource) waspApp.getWaspmoteSqlHelper().getUserDataSource(this);
		List<User> users = userData.getAllUsers();
		for(User user : users)
		{
			mAdapter.add(user.getUsername()+":"+" "+user.getFirstName()+" "+user.getLastName());
		}
		userDialog.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//dohvati username -> splitaj po : i uzmi 0-ti element
				String[] usernameSplit = mAdapter.getItem(which).split(":");
				final String username = usernameSplit[0];
				AlertDialog.Builder confirm = new AlertDialog.Builder(AdministrationActivity.this);
				confirm.setTitle("Warning");
				confirm.setMessage("Are you sure you want to delete this user?");
				confirm.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(username.equals(waspApp.getCurrentUser().getUsername()))
						{
							AlertDialog.Builder notPossible = new AlertDialog.Builder(AdministrationActivity.this);
							notPossible.setTitle("Warning");
							notPossible.setMessage("It is not possible to delete user that is logged!!");
							notPossible.setPositiveButton("Ok", null);
							notPossible.show();
							
						}
						else
						{
							userData.deleteUser(userData.getUserByUsername(username));
						}
					}
				});
				confirm.setNegativeButton("Cancel", null);
				confirm.show();
			}
		});
		AlertDialog alertDialogUsers = userDialog.create();
		alertDialogUsers.show();
		
	}

}
