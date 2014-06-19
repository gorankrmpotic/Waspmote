package hr.fer.zari.waspmote.db.dao;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.db.tables.SensorsTable;
import hr.fer.zari.waspmote.models.SensorType;
import hr.fer.zari.waspmote.models.Sensors;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SensorsDataSource implements ITableDataSource{

	private static final String TAG = "SensorsDataSource";
	private SQLiteDatabase database;
	private WaspmoteSQLiteHelper dbHelper;
	private String[] allColumns = SensorsTable.COLUMNS;
	
	public SensorsDataSource(Context context) {
		dbHelper = new WaspmoteSQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public boolean SensorExists(String sensorName)
	{
		this.open();
		Cursor cursor = database.query(SensorsTable.TABLE_SENSORS, SensorsTable.COLUMNS, SensorsTable.COLUMN_SENSOR_NAME+" = ?", new String[]{sensorName}, null, null, null);
		if(cursor.getCount() == 0)
		{
			this.close();
			return false;
		}		
		this.close();
		return true;
	}
	
	public void addSensor(String sensorName, int sensorType)
	{
		if(SensorExists(sensorName))
		{
			//update
			updateSensor(sensorName, sensorType, sensorName);
			return;
		}
		ContentValues values = new ContentValues();
		values.put(SensorsTable.COLUMN_SENSOR_NAME, sensorName);
		values.put(SensorsTable.COLUMN_SENSOR_TYPE, sensorType);
		this.open();
		database.insert(SensorsTable.TABLE_SENSORS, null, values);
		this.close();		
	}
	
	public void updateSensor(String sensorName, int sensorType, String oldSensorName)
	{
		Sensors sensorToUpdate = getSensorByName(oldSensorName);
		ContentValues values = new ContentValues();
		values.put(SensorsTable.COLUMN_SENSOR_NAME, sensorName);
		values.put(SensorsTable.COLUMN_SENSOR_TYPE, sensorType);
		this.open();
		database.update(SensorsTable.TABLE_SENSORS, values, SensorsTable.COLUMN_ID+" = ?", new String[]{String.valueOf(sensorToUpdate.get_id())});
		this.close();
	}
	
	public Sensors getSensorByName(String sensorName)
	{
		if(SensorExists(sensorName))
		{
			this.open();
			Cursor cursor = database.query(SensorsTable.TABLE_SENSORS, SensorsTable.COLUMNS, SensorsTable.COLUMN_SENSOR_NAME+" = ?", new String[]{sensorName}, null, null, null);
			if(cursor != null)
			{
				cursor.moveToFirst();
			}
			Sensors sens = new Sensors(Integer.parseInt(cursor.getString(0)), cursor.getString(1),Integer.parseInt(cursor.getString(2)));
			this.close();
			return sens;
		}
		return null;
	}
	
	public Sensors getSensorById(int sensorId)
	{
		this.open();
		Cursor cursor = database.query(SensorsTable.TABLE_SENSORS, SensorsTable.COLUMNS, SensorsTable.COLUMN_ID+" = ?", new String[]{String.valueOf(sensorId)}, null, null, null);
		if(cursor != null)
		{
			cursor.moveToFirst();
		}
		Sensors sens = new Sensors(Integer.parseInt(cursor.getString(0)), cursor.getString(1),Integer.parseInt(cursor.getString(2)));
		this.close();
		return sens;
	}
	
	public void deleteSensor(Sensors sensor)
	{
		this.open();
		database.delete(SensorsTable.TABLE_SENSORS, SensorsTable.COLUMN_ID+" = ?", new String[]{String.valueOf(sensor.get_id())});
		this.close();
	}
	
	public List<Sensors> getAllSensors()
	{
		List<Sensors> listSensors = new LinkedList<Sensors>();
		String query = "SELECT * FROM "+SensorsTable.TABLE_SENSORS;
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		Sensors sens = null;
		if (cursor.moveToFirst()) {
            do {            
            	sens = new Sensors(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Integer.parseInt(cursor.getString(2)));
            	listSensors.add(sens);
            } while (cursor.moveToNext());
        }		
		this.close();
		return listSensors;
	}
	
	public List<Sensors> getAllSensorsBySensorType(int sensorTypeId)
	{
		List<Sensors> listSensors = new LinkedList<Sensors>();
		this.open();
		Cursor cursor = database.query(SensorsTable.TABLE_SENSORS, SensorsTable.COLUMNS, SensorsTable.COLUMN_SENSOR_TYPE +" = ?", new String[]{String.valueOf(sensorTypeId)}, null, null, null);
		Sensors sens = null;
		if(cursor.moveToFirst())
		{
			do
			{
				sens = new Sensors(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Integer.parseInt(cursor.getString(2)));
				listSensors.add(sens);
			} while(cursor.moveToNext());
		}
		this.close();
		return listSensors;
	}
}
