package hr.fer.zari.waspmote.db.tables;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserTable {

	// ********* USER TALBE ******************
	public static final String TABLE_USER = "User";

	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FIRST_NAME = "firstName";
	public static final String COLUMN_LAST_NAME = "lastName";
	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_PASSWORD = "password";

	// ALL COLUMNS
	public static final String[] COLUMNS = { COLUMN_ID, COLUMN_FIRST_NAME,
			COLUMN_LAST_NAME, COLUMN_USERNAME, COLUMN_PASSWORD };

	// CREATE TABLE SQL STATEMENT
	public static final String CREATE_TABLE_USER =
			"CREATE TABLE User ( "
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_FIRST_NAME + " TEXT, "
			+ COLUMN_LAST_NAME + " TEXT, "
			+ COLUMN_USERNAME + " TEXT UNIQUE, "
			+ COLUMN_PASSWORD + " TEXT )";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_USER);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(UserTable.class.getSimpleName(), "onUpdate");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
		onCreate(database);
	}
}
