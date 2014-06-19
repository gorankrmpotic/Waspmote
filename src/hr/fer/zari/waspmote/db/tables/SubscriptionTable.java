package hr.fer.zari.waspmote.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class SubscriptionTable {
	
	public static final String TABLE_SUBSCRIPTION = "Subscription";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ID_GSN = "idGSN";
	public static final String COLUMN_PERIOD = "period";
	
	public static final String[] COLUMNS = {COLUMN_ID, COLUMN_ID_GSN, COLUMN_PERIOD};
	
	public static final String CREATE_TABLE_SUBSCRIPTION = "CREATE TABLE "+TABLE_SUBSCRIPTION
			+ " ( "
			+ COLUMN_ID
			+" INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_ID_GSN
			+" INTEGER, "
			+ COLUMN_PERIOD
			+" INTEGER,"
			+" FOREIGN KEY("+COLUMN_ID_GSN+") REFERENCES "+GSNTable.TABLE_GSN+"("+GSNTable.COLUMN_ID+") ON DELETE CASCADE"+")";
	
	public static void onCreate(SQLiteDatabase database) 
	{
		database.execSQL(CREATE_TABLE_SUBSCRIPTION);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBSCRIPTION);
		onCreate(database);
	}
}
