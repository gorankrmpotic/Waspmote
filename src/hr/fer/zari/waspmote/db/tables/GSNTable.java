package hr.fer.zari.waspmote.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class GSNTable {

	public static final String TABLE_GSN = "GSN";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_IP = "ip";
	public static final String COLUMN_GSN_NAME = "GSNName";
	public static final String COLUMN_GSN_USERNAME = "GSNUsername";
	public static final String COLUMN_GSN_PASSWORD = "GSNPassword";
	
	public static final String[] COLUMNS = {COLUMN_ID, COLUMN_IP, COLUMN_GSN_NAME, COLUMN_GSN_USERNAME, COLUMN_GSN_PASSWORD};
	
	public static final String CREATE_TABLE_GSN = "CREATE TABLE " +TABLE_GSN+
			" ( "
			+ COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_IP
			+ " TEXT, "
			+ COLUMN_GSN_NAME
			+ " TEXT UNIQUE, "
			+ COLUMN_GSN_USERNAME
			+" TEXT, "
			+COLUMN_GSN_PASSWORD
			+" TEXT)";
	
	public static void onCreate(SQLiteDatabase database) 
	{
		database.execSQL(CREATE_TABLE_GSN);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_GSN);
		onCreate(database);
	}
	
	
}
