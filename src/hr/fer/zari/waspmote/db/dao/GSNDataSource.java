package hr.fer.zari.waspmote.db.dao;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.db.tables.GSNTable;
import hr.fer.zari.waspmote.models.GSN;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class GSNDataSource implements ITableDataSource{

	private static final String TAG = "GSNDataSource";
	private SQLiteDatabase database;
	private WaspmoteSQLiteHelper dbHelper;
	private String[] allColumns = GSNTable.COLUMNS;
	
	public GSNDataSource(Context context)
	{
		dbHelper = new WaspmoteSQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public boolean GSNExists(String GSNname)
	{
		this.open();
		Cursor cursor = database.query(GSNTable.TABLE_GSN, GSNTable.COLUMNS, GSNTable.COLUMN_GSN_NAME+" = ?", new String[]{GSNname}, null, null, null);
		if(cursor.getCount() == 0)
		{
			this.close();
			return false;
		}		
		this.close();
		return true;
	}
	
	public void addGSN(String ip, String GSNname, String username, String password)
	{
		if(GSNExists(GSNname))
		{
			//update
			updateGSN(ip, GSNname, username, password, GSNname);
			return;
		}
		ContentValues values = new ContentValues();
		values.put(GSNTable.COLUMN_IP, ip);
		values.put(GSNTable.COLUMN_GSN_NAME, GSNname);
		values.put(GSNTable.COLUMN_GSN_USERNAME, username);
		values.put(GSNTable.COLUMN_GSN_PASSWORD, password);
		this.open();
		database.insert(GSNTable.TABLE_GSN, null, values);
		this.close();
	}
	
	public void updateGSN(String ip, String GSNname, String username, String password, String oldGSNname)
	{
		GSN GSNToUpdate = getGSNByName(oldGSNname);
		ContentValues values = new ContentValues();
		values.put(GSNTable.COLUMN_IP, ip);
		values.put(GSNTable.COLUMN_GSN_NAME, GSNname);
		values.put(GSNTable.COLUMN_GSN_USERNAME, username);
		values.put(GSNTable.COLUMN_GSN_PASSWORD, password);
		this.open();
		database.update(GSNTable.TABLE_GSN, values, GSNTable.COLUMN_ID+" = ?", new String[]{String.valueOf(GSNToUpdate.get_id())});
		this.close();
	}
	
	public GSN getGSNByName(String GSNname)
	{
		if(GSNExists(GSNname))
		{
			this.open();
			Cursor cursor = database.query(GSNTable.TABLE_GSN, GSNTable.COLUMNS, GSNTable.COLUMN_GSN_NAME+" = ?", new String[]{GSNname}, null, null, null);
			if(cursor != null)
			{
				cursor.moveToFirst();
			}
			GSN gsn = new GSN(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
			this.close();
			return gsn;
		}
		return null;
	}
	
	public void deleteGSN(GSN gsn)
	{
		this.open();
		database.delete(GSNTable.TABLE_GSN, GSNTable.COLUMN_ID+" = ?", new String[]{String.valueOf(gsn.get_id())});
		this.close();
	}
	
	public List<GSN> getAllGSN()
	{
		List<GSN> listGSNs = new LinkedList<GSN>();
		String query = "SELECT * FROM "+GSNTable.TABLE_GSN;
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		GSN gsn = null;
		if (cursor.moveToFirst()) {
            do {           
            	gsn = new GSN(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            	listGSNs.add(gsn);
            } while (cursor.moveToNext());
        }
		this.close();
		return listGSNs;
	}
}
