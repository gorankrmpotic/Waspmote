package hr.fer.zari.waspmote.db.dao;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.db.tables.SensorTypeTable;
import hr.fer.zari.waspmote.models.SensorType;
import hr.fer.zari.waspmote.models.User;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SensorTypeDataSource implements ITableDataSource {
	
	private static final String TAG = "SensorTypeDataSource";
	private SQLiteDatabase database;
	private WaspmoteSQLiteHelper dbHelper;
	private String[] allColumns = SensorTypeTable.COLUMNS;
	
	public SensorTypeDataSource(Context context) {
		dbHelper = new WaspmoteSQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public boolean typeExists(String sensorType)
	{
		this.open();
		Cursor cursor = database.query(SensorTypeTable.TABLE_SENSOR_TYPE, SensorTypeTable.COLUMNS, SensorTypeTable.COLUMN_TYPE_NAME+ " = ?", new String[]{sensorType}, null, null, null);
		if(cursor.getCount() == 0)
		{
			this.close();
			return false;
		}
		this.close();
		return true;
	}
	
	public void addSensorType(String sensorType)
	{
		if(typeExists(sensorType))
		{			
			updateSensorType(sensorType, sensorType);
			return;
		}
		ContentValues values = new ContentValues();
		values.put(SensorTypeTable.COLUMN_TYPE_NAME, sensorType);
		this.open();
		database.insert(SensorTypeTable.TABLE_SENSOR_TYPE, null, values);
		this.close();	
	}
	
	public void updateSensorType(String sensorType, String oldSensorType)
	{
		SensorType sensorTypeToUpdate = getSensorTypeByName(oldSensorType);
		ContentValues values = new ContentValues();
		values.put(SensorTypeTable.COLUMN_TYPE_NAME, sensorType);
		this.open();
		database.update(SensorTypeTable.TABLE_SENSOR_TYPE, values, SensorTypeTable.COLUMN_ID+" = ?", new String[]{String.valueOf(sensorTypeToUpdate.get_id())});
		this.close();
	}
	
	public SensorType getSensorTypeByName(String sensorType)
	{
		if(typeExists(sensorType))
		{
			this.open();
			Cursor cursor = database.query(SensorTypeTable.TABLE_SENSOR_TYPE, SensorTypeTable.COLUMNS, SensorTypeTable.COLUMN_TYPE_NAME +" = ?", new String[]{sensorType}, null, null, null);
			if(cursor != null)
			{
				cursor.moveToFirst();
			}
			SensorType sType = new SensorType(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
			this.close();
			return sType;
		}
		return null;
	}
	
	public void deleteSensorType(SensorType sType)
	{
		this.open();
		database.delete(SensorTypeTable.TABLE_SENSOR_TYPE, SensorTypeTable.COLUMN_ID+" = ?", new String[]{String.valueOf(sType.get_id())});		
		this.close();
	}
	
	public List<SensorType> getAllSensorTypes()
	{
		List<SensorType> sensorTypes = new LinkedList<SensorType>();
		String query = "SELECT * FROM "+ SensorTypeTable.TABLE_SENSOR_TYPE;
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		SensorType sType = null;
		if (cursor.moveToFirst()) {
            do {
            	sType = new SensorType(Integer.parseInt(cursor.getString(0)), cursor.getString(1));                               
                sensorTypes.add(sType);
            } while (cursor.moveToNext());
        }		
		this.close();
		return sensorTypes;
	}

}
