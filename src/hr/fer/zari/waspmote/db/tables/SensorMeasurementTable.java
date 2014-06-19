package hr.fer.zari.waspmote.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class SensorMeasurementTable {

	
	public static final String TABLE_SENSOR_MEASUREMENT = "SensorMeasurement";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ID_SENSOR = "idSensor";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_VALUE = "value";
	public static final String COLUMN_MEASUREMENT_UNIT = "measurementUnit";
	
	public static final String[] COLUMNS = {COLUMN_ID, COLUMN_ID_SENSOR, COLUMN_TIMESTAMP, COLUMN_VALUE, COLUMN_MEASUREMENT_UNIT};
	
	public static final String CREATE_TABLE_SENSOR_MEASUREMENTS = "CREATE TABLE " + TABLE_SENSOR_MEASUREMENT
			+ " ( "
			+ COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_ID_SENSOR
			+ " INTEGER, "
			+ COLUMN_TIMESTAMP
			+ " LONG, "
			+ COLUMN_VALUE
			+ " TEXT, "
			+ COLUMN_MEASUREMENT_UNIT
			+ " TEXT, "
			+ " FOREIGN KEY("+COLUMN_ID_SENSOR+") REFERENCES "+SensorsTable.TABLE_SENSORS+"("+SensorsTable.COLUMN_ID+")"+")";
	
	public static void onCreate(SQLiteDatabase database) 
	{
		database.execSQL(CREATE_TABLE_SENSOR_MEASUREMENTS);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_MEASUREMENT);
		onCreate(database);
	}
}
