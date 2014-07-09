package hr.fer.zari.waspmote;



import hr.fer.zari.waspmote.db.dao.UserDataSource;
import hr.fer.zari.waspmote.models.User;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class LogInActivity extends ActionBarActivity {

//	public String username = "admin";
//	private String password = "admin";
	
	WaspmoteApplication waspApp;
	UserDataSource userData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		setTitle("Log In");
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		waspApp = (WaspmoteApplication)getApplication();
		userData = (UserDataSource)waspApp.getWaspmoteSqlHelper().getUserDataSource(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_log_in,
					container, false);
			return rootView;
		}
	}
	
	public void OkButtonClicked(View view)
	{
		//potrebno napraviti provjeru Usernamea i passworda i proslijediti na MainApplicationActivity ili kako god ga nazovem
		//ako provjera pro�e napraviti ovo, ako ne onda ponoviti login!
		//privremena provjera, promjeniti ovo kad se spoji baza!!
		EditText usernameInput = (EditText)findViewById(R.id.UsernameEditText);
		EditText passwordInput = (EditText)findViewById(R.id.PasswordEditText);
		User userLogged = getLoginUserData(usernameInput.getText().toString());
//		if(usernameInput.getText().toString().equals(username) && passwordInput.getText().toString().equals(password))
		if(userLogged != null)
		{
			if(userLogged.getPassword().equals(passwordInput.getText().toString()))
			{
				waspApp.setCurrentUser(userLogged);
				Intent MainIntent = new Intent(this, MainActivity.class);
				//MainIntent.putExtra("username", usernameInput.getText().toString());			
				startActivity(MainIntent);		
				finish();
			}
			else
			{
				AlertDialog.Builder adb = new AlertDialog.Builder(LogInActivity.this);
				adb.setTitle("Login Failed");				
				adb.setMessage("You entered wrong username/password!");			
				adb.setPositiveButton("Ok", null);
				adb.show();    
			}
		}
		else
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(LogInActivity.this);
			adb.setTitle("Login Failed");				
			adb.setMessage("You entered non existing username!");			
			adb.setPositiveButton("Ok", null);
			adb.show();
		}
		//u redu je ovako da se ubije trenutni activity jer onda prilikom backa samo baci na početni zaslon :)
		
	}
	
	public void CancelButtonClicked(View view)
	{
		finish();
	}
	
	public User getLoginUserData(String username)
	{
		try
		{
			//return userData.getUserByUsername(username);
			//return sql.getUser(username);
			return userData.getUserByUsername(username);
		}
		catch(Exception ex)
		{
			//do ovoga dolazi ako se ništa ne pronaće zasad!
			AlertDialog.Builder aa = new AlertDialog.Builder(LogInActivity.this);
			aa.setTitle("Exception2");
			aa.setMessage(ex.getMessage());
			aa.setPositiveButton("OK", null);
			aa.show();
		}
		
		return null;
	}

}
