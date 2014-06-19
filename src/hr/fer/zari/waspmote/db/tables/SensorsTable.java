package hr.fer.zari.waspmote.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class SensorsTable {
	
	public static final String TABLE_SENSORS = "Sensors";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SENSOR_NAME = "sensorName";
	public static final String COLUMN_SENSOR_TYPE = "sensorType";
	
	public static final String[] COLUMNS = { COLUMN_ID, COLUMN_SENSOR_NAME,
		COLUMN_SENSOR_TYPE };
	
	public static final String CREATE_TABLE_SENSORS = "CREATE TABLE "
			+ TABLE_SENSORS + " ( "
			+ COLUMN_ID	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_SENSOR_NAME + " TEXT UNIQUE, "
			+ COLUMN_SENSOR_TYPE + " INTEGER,"
			+ " FOREIGN KEY("+COLUMN_SENSOR_TYPE+")"+ " REFERENCES "+SensorTypeTable.TABLE_SENSOR_TYPE+"("+SensorTypeTable.COLUMN_ID+")" +")";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_SENSORS);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSORS);
		onCreate(database);
	}
	

}
