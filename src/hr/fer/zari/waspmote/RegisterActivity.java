package hr.fer.zari.waspmote;


import hr.fer.zari.waspmote.db.dao.UserDataSource;
import hr.fer.zari.waspmote.models.User;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends ActionBarActivity {

	String ActivityType;	
	WaspmoteApplication waspApp;
	UserDataSource userData;
	String editUser = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		//setTitle("Register");
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		waspApp = (WaspmoteApplication)getApplication();
		userData =(UserDataSource) waspApp.getWaspmoteSqlHelper().getUserDataSource(this);
		Bundle bundle = getIntent().getExtras();
		ActivityType = bundle.getString("Type");
		Button acceptButton = (Button)findViewById(R.id.SubmitButton);			
		switch (ActivityType) {		
		case "Create":
			setTitle("Create User");
			acceptButton.setText("Create");				
			break;
		case "Edit":
			setTitle("Edit User");
			acceptButton.setText("Change");	
			editUser = bundle.getString("Username");
			EditText name = (EditText) findViewById(R.id.NameEditText);
			EditText surname = (EditText) findViewById(R.id.SurnameEditText);			
			EditText username = (EditText) findViewById(R.id.RegisterUsernameEditText);
			EditText password = (EditText) findViewById(R.id.RegisterPasswordEditText);
			EditText passwordRetype = (EditText) findViewById(R.id.RegisterRetypePasswordEditText);
			
			//username.setText(editUser, TextView.BufferType.EDITABLE);
			User userOldData = userData.getUserByUsername(editUser);
			name.setText(userOldData.getFirstName());
			surname.setText(userOldData.getLastName());			
			username.setText(editUser);
			password.setText(userOldData.getPassword());
			passwordRetype.setText(userOldData.getPassword());		
			
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_register,
					container, false);
			return rootView;
		}
	}
	
	public void SubmitButtonClicked(View view)
	{
		//validiraj sve unose i prihvati/odbij registraciju!
		//ako prihvati� registraciju odi na mainActivity, ako ne ostani ovdje sa starim unosima
		//ako pro�e validacija radi ovo
		
		EditText nameInput = (EditText)findViewById(R.id.NameEditText);
		EditText surnameInput = (EditText)findViewById(R.id.SurnameEditText);
		EditText usernameInput = (EditText)findViewById(R.id.RegisterUsernameEditText);
		EditText passwordInput = (EditText)findViewById(R.id.RegisterPasswordEditText);
		EditText passwordRetypeInput = (EditText)findViewById(R.id.RegisterRetypePasswordEditText);
		if(nameInput.getText().toString().isEmpty() || surnameInput.getText().toString().isEmpty() || usernameInput.getText().toString().isEmpty() || passwordInput.getText().toString().isEmpty() || passwordRetypeInput.getText().toString().isEmpty())
		{
			AlertDialog.Builder emptyField = new AlertDialog.Builder(RegisterActivity.this);
			emptyField.setTitle("Error");
			emptyField.setMessage("You need to fill all fields!!");
			emptyField.setPositiveButton("Ok", null);
			emptyField.show();					
		}
		else if(passwordInput.getText().toString().equals(passwordRetypeInput.getText().toString()))
		{
			if(!editUser.isEmpty())
			{
				if(userData.userExists(usernameInput.getText().toString()) && !usernameInput.getText().toString().equals(editUser))
				{
					AlertDialog.Builder userExists = new AlertDialog.Builder(RegisterActivity.this);
					userExists.setTitle("Error");
					userExists.setMessage("User with that username already exists!");
					userExists.setPositiveButton("Ok", null);
					userExists.show();						
				}
				else
				{
					userData.updateUser(nameInput.getText().toString(), surnameInput.getText().toString(), usernameInput.getText().toString(), passwordInput.getText().toString(), editUser);
					if(waspApp.getCurrentUser().getUsername().equals(editUser))
					{
						waspApp.setCurrentUser(userData.getUserByUsername(usernameInput.getText().toString()));
					}
					finish();
				}			
			}
			else
			{
				if(userData.userExists(usernameInput.getText().toString()))
				{
					AlertDialog.Builder userExists = new AlertDialog.Builder(RegisterActivity.this);
					userExists.setTitle("Error");
					userExists.setMessage("User with that username already exists!");
					userExists.setPositiveButton("Ok", null);
					userExists.show();						
				}
				else
				{
					userData.addUser(nameInput.getText().toString(), surnameInput.getText().toString(), usernameInput.getText().toString(), passwordInput.getText().toString());
					finish();
				}
			}
		}
		else
		{
			AlertDialog.Builder dataError = new AlertDialog.Builder(RegisterActivity.this);
			dataError.setTitle("Error");
			dataError.setMessage("Passwords don't match");
			dataError.setPositiveButton("Ok", null);
			dataError.show();
		}
	}
	
	public void RegisterCancelButtonClicked(View view)
	{
		finish();
	}

}
