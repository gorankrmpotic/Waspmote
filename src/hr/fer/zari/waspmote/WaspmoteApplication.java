package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.models.User;
import android.app.Application;

public class WaspmoteApplication extends Application {

	//private static final String TAG = WaspmoteApplication.class.getSimpleName();
	private WaspmoteSQLiteHelper dbHelper;
	private User currentUser = null;

	@Override
	public void onCreate() {
		super.onCreate();
		dbHelper = new WaspmoteSQLiteHelper(getApplicationContext());
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		// za svaki slučaj ovdje zatvaramo eventualno otvorene veze prema bazi
		dbHelper.close();
	}

	public WaspmoteSQLiteHelper getWaspmoteSqlHelper() {
		return dbHelper;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	
	public User getCurrentUser()
	{
		return currentUser;
	}
	
	public void logOut() {
		this.currentUser = null;
	}
}
