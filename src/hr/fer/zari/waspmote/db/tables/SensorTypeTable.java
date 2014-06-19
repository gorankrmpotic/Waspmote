package hr.fer.zari.waspmote.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class SensorTypeTable {
	
	
	
	public static final String TABLE_SENSOR_TYPE = "SensorType";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TYPE_NAME = "typeName";
	
	public static final String[] COLUMNS = { COLUMN_ID, COLUMN_TYPE_NAME };
	
	public static final String CREATE_TABLE_SENSOR_TYPE = "CREATE TABLE " + TABLE_SENSOR_TYPE +
			" ( "
			+ COLUMN_ID 
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_TYPE_NAME + " TEXT UNIQUE )";
			
	
	public static void onCreate(SQLiteDatabase database) 
	{
		database.execSQL(CREATE_TABLE_SENSOR_TYPE);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_TYPE);
		onCreate(database);
	}
}
