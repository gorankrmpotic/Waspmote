package hr.fer.zari.waspmote.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class SensorSubscriptionTable {

	public static final String TABLE_SENSOR_SUBSCRIPTION = "SensorSubscription";
	
	public static final String COLUMN_ID_SUBSCRIPTION = "_idSubscription";
	public static final String COLUMN_ID_SENSOR = "_idSensor";
	
	public static final String[] COLUMNS = {COLUMN_ID_SUBSCRIPTION, COLUMN_ID_SENSOR};
	
	public static final String CREATE_TABLE_SENSOR_SUBSCRIPTION = "CREATE TABLE "+TABLE_SENSOR_SUBSCRIPTION
			+ " ( "
			+ COLUMN_ID_SUBSCRIPTION
			+ " INTEGER, "
			+ COLUMN_ID_SENSOR
			+ " INTEGER, "
			+ " PRIMARY KEY ("+COLUMN_ID_SUBSCRIPTION+", "+COLUMN_ID_SENSOR+"), "
			+ " FOREIGN KEY("+COLUMN_ID_SUBSCRIPTION+") REFERENCES "+SubscriptionTable.TABLE_SUBSCRIPTION+"("+SubscriptionTable.COLUMN_ID+") ON DELETE CASCADE, "
			+ " FOREIGN KEY("+COLUMN_ID_SENSOR+") REFERENCES "+SensorsTable.TABLE_SENSORS+"("+SensorsTable.COLUMN_ID+")"+")";
	
	public static void onCreate(SQLiteDatabase database) 
	{
		database.execSQL(CREATE_TABLE_SENSOR_SUBSCRIPTION);	
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		database.execSQL("DROP TABLE IF EXISTS "+TABLE_SENSOR_SUBSCRIPTION);
		onCreate(database);
	}
}
