package hr.fer.zari.waspmote.db.dao;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.db.tables.SubscriptionTable;
import hr.fer.zari.waspmote.models.Subscription;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SubscriptionDataSource implements ITableDataSource{
	
	private static final String TAG = "SubscriptionDataSource";
	private SQLiteDatabase database;
	private WaspmoteSQLiteHelper dbHelper;
	private String[] allColumns = SubscriptionTable.COLUMNS;
	
	public SubscriptionDataSource(Context context)
	{
		dbHelper = new WaspmoteSQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void addSubscription(int idGSN, int period)
	{
		ContentValues values = new ContentValues();
		values.put(SubscriptionTable.COLUMN_ID_GSN, idGSN);
		values.put(SubscriptionTable.COLUMN_PERIOD, period);
		this.open();
		database.insert(SubscriptionTable.TABLE_SUBSCRIPTION, null, values);
		this.close();
	}
	
	public void deleteSubscription(Subscription subscription)
	{
		this.open();
		database.delete(SubscriptionTable.TABLE_SUBSCRIPTION, SubscriptionTable.COLUMN_ID+" = ?", new String[]{String.valueOf(subscription.get_id())});
		this.close();
	}
	
	// trenutno splikscija radi samo s jednim subscriptionom
	public List<Subscription> getAllSubscriptions()
	{
		List<Subscription> listSubscriptions = new LinkedList<Subscription>();
		String query = "SELECT * FROM "+SubscriptionTable.TABLE_SUBSCRIPTION;
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		Subscription subs = null;
		if (cursor.moveToFirst()) {
            do {      
            	subs = new Subscription(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)));
            	listSubscriptions.add(subs);
            } while (cursor.moveToNext());
        }
		this.close();
		return listSubscriptions;
	}

}
