package hr.fer.zari.waspmote.db.dao;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.db.tables.SensorSubscriptionTable;
import hr.fer.zari.waspmote.models.SensorSubscription;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SensorSubscriptionDataSource implements ITableDataSource {
	
	private static final String TAG = "SensorSubscriptionDataSource";
	private SQLiteDatabase database;
	private WaspmoteSQLiteHelper dbHelper;
	private String[] allColumns = SensorSubscriptionTable.COLUMNS;
	
	public SensorSubscriptionDataSource(Context context)
	{		
		dbHelper = new WaspmoteSQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void addSensorSubscription(int idSubscription, int idSensor)
	{
		ContentValues values = new ContentValues();
		values.put(SensorSubscriptionTable.COLUMN_ID_SUBSCRIPTION, idSubscription);
		values.put(SensorSubscriptionTable.COLUMN_ID_SENSOR, idSensor);
		this.open();
		database.insert(SensorSubscriptionTable.TABLE_SENSOR_SUBSCRIPTION, null, values);
		this.close();
	}
	
	public void deleteSensorSubscription(SensorSubscription sensorSubscription)
	{
		this.open();
		database.delete(SensorSubscriptionTable.TABLE_SENSOR_SUBSCRIPTION, SensorSubscriptionTable.COLUMN_ID_SUBSCRIPTION+" = ? AND "+SensorSubscriptionTable.COLUMN_ID_SENSOR+" = ?", new String[]{String.valueOf(sensorSubscription.get_idSubscription()), String.valueOf(sensorSubscription.get_idSensor())});
		this.close();
	}
	
	public List<SensorSubscription> getAllSensorSubscriptions()
	{
		List<SensorSubscription> listSensorSubscriptions = new LinkedList<SensorSubscription>();
		String query = "SELECT * FROM "+SensorSubscriptionTable.TABLE_SENSOR_SUBSCRIPTION;
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		SensorSubscription sensSub = null;
		if (cursor.moveToFirst()) {
            do { 
            	sensSub = new SensorSubscription(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)));
            	listSensorSubscriptions.add(sensSub);
            } while (cursor.moveToNext());
        }		
		this.close();
		return listSensorSubscriptions;
	}
	
	public List<SensorSubscription> getAllSensorSubscriptionsBySensorId(int sensorId)
	{
		List<SensorSubscription> listSensorSubscriptions = new LinkedList<SensorSubscription>();
		this.open();
		Cursor cursor = database.query(SensorSubscriptionTable.TABLE_SENSOR_SUBSCRIPTION, SensorSubscriptionTable.COLUMNS, SensorSubscriptionTable.COLUMN_ID_SENSOR+" = ?", new String[]{String.valueOf(sensorId)}, null, null, null);
		SensorSubscription sensSub = null;
		if (cursor.moveToFirst()) {
            do { 
            	sensSub = new SensorSubscription(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)));
            	listSensorSubscriptions.add(sensSub);
            } while (cursor.moveToNext());
        }	
		this.close();
		return listSensorSubscriptions;
	}
	
	public List<SensorSubscription> getAllSensorSubscriptionsBySubscriptionId(int subscriptionId)
	{
		List<SensorSubscription> listSensorSubscriptions = new LinkedList<SensorSubscription>();
		this.open();
		Cursor cursor = database.query(SensorSubscriptionTable.TABLE_SENSOR_SUBSCRIPTION, SensorSubscriptionTable.COLUMNS, SensorSubscriptionTable.COLUMN_ID_SUBSCRIPTION+" = ?", new String[]{String.valueOf(subscriptionId)}, null, null, null);
		SensorSubscription sensSub = null;
		if (cursor.moveToFirst()) {
            do { 
            	sensSub = new SensorSubscription(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)));
            	listSensorSubscriptions.add(sensSub);
            } while (cursor.moveToNext());
        }	
		this.close();
		return listSensorSubscriptions;
	}

}
