package hr.fer.zari.waspmote.db.dao;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.db.tables.SensorMeasurementTable;
import hr.fer.zari.waspmote.models.SensorMeasurement;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;

public class SensorMeasurementDataSource implements ITableDataSource{

	private static final String TAG = "SensorMeasurementDataSource";
	private SQLiteDatabase database;
	private WaspmoteSQLiteHelper dbHelper;
	private String[] allColumns = SensorMeasurementTable.COLUMNS;
	
	
	public SensorMeasurementDataSource(Context context)
	{
		dbHelper = new WaspmoteSQLiteHelper(context);		
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void addSensorMeasurement(int idSensor, long timestamp, String value, String measurementUnit)
	{
		ContentValues values = new ContentValues();
		values.put(SensorMeasurementTable.COLUMN_ID_SENSOR, idSensor);
		values.put(SensorMeasurementTable.COLUMN_TIMESTAMP, timestamp);
		values.put(SensorMeasurementTable.COLUMN_VALUE, value);
		values.put(SensorMeasurementTable.COLUMN_MEASUREMENT_UNIT, measurementUnit);
		this.open();
		database.insert(SensorMeasurementTable.TABLE_SENSOR_MEASUREMENT, null, values);
		this.close();		
	}
	
	public void deleteSensorMeasurement(SensorMeasurement sensorMeasurement)
	{
		this.open();
		database.delete(SensorMeasurementTable.TABLE_SENSOR_MEASUREMENT, SensorMeasurementTable.COLUMN_ID+" = ?", new String[]{String.valueOf(sensorMeasurement.get_id())});
		this.close();
	}
	
	public List<SensorMeasurement> getAllSensorMeasurement()
	{
		List<SensorMeasurement> listMeasurements = new LinkedList<SensorMeasurement>();
		String query = "SELECT * FROM "+SensorMeasurementTable.TABLE_SENSOR_MEASUREMENT;
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		SensorMeasurement measure = null;
		if (cursor.moveToFirst()) {
            do {          	
            	//measure = new SensorMeasurement(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)), cursor.getString(3), cursor.getString(4));
            	measure = new SensorMeasurement(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)), Long.parseLong(cursor.getString(2)), cursor.getString(3), cursor.getString(4));
            	listMeasurements.add(measure);
            } while (cursor.moveToNext());
        }
		this.close();
		return listMeasurements;
	}
	
	public List<SensorMeasurement> getAllSensorMeasurementByTimestamp(long timestamp)
	{
		List<SensorMeasurement> listMeasurements = new LinkedList<SensorMeasurement>();
		this.open();
		Cursor cursor = database.query(SensorMeasurementTable.TABLE_SENSOR_MEASUREMENT, SensorMeasurementTable.COLUMNS, SensorMeasurementTable.COLUMN_TIMESTAMP+" = ?", new String[]{String.valueOf(timestamp)}, null, null, null);
		SensorMeasurement measure = null;
		if (cursor.moveToFirst()) {
            do {          	
//            	measure = new SensorMeasurement(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)), cursor.getString(3), cursor.getString(4));
            	measure = new SensorMeasurement(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)), Long.parseLong(cursor.getString(2)), cursor.getString(3), cursor.getString(4));
            	listMeasurements.add(measure);
            } while (cursor.moveToNext());
        }
		this.close();
		return listMeasurements;
	}
	
	public void DeleteAllSensorMeasurements()
	{
		this.open();
		database.execSQL("DELETE FROM "+SensorMeasurementTable.TABLE_SENSOR_MEASUREMENT);
		this.close();
	}
	
	public long getMaxTimestamp()
	{
		if(getAllSensorMeasurement().size() == 0)
		{
			return -1;
		}
		this.open();		
		Cursor cursor = database.query(SensorMeasurementTable.TABLE_SENSOR_MEASUREMENT, new String[]{"MAX("+SensorMeasurementTable.COLUMN_TIMESTAMP+")"}, null, null, null, null, null);
		if(cursor != null)
		{
			cursor.moveToFirst();
		}		
		this.close();
		return Long.parseLong(cursor.getString(0));
	}
}
