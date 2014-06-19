package hr.fer.zari.waspmote.db.dao;

import hr.fer.zari.waspmote.db.WaspmoteSQLiteHelper;
import hr.fer.zari.waspmote.db.tables.UserTable;
import hr.fer.zari.waspmote.models.User;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserDataSource implements ITableDataSource {

	private static final String TAG = "UserDataSource";
	private SQLiteDatabase database;
	private WaspmoteSQLiteHelper dbHelper;
	private String[] allColumns = UserTable.COLUMNS;

	public UserDataSource(Context context) {
		dbHelper = new WaspmoteSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}

	/**
	 * Dodaje novog korisnika u bazu ili ažurira postojećeg korisnika.
	 * 
	 * @param firstName
	 * @param lastName
	 * @param username
	 * @param password
	 * @return id ubačenog korisnika ili -1 ukoliko je došlo do greške ili
	 *         korisnik već postoji
	 */
	public long addOrUpdateUser(String firstName, String lastName,
			String username, String password) {
		// crete values
		ContentValues values = new ContentValues();		
		values.put(UserTable.COLUMN_FIRST_NAME, firstName);
		values.put(UserTable.COLUMN_LAST_NAME, lastName);
		values.put(UserTable.COLUMN_USERNAME, username);
		values.put(UserTable.COLUMN_PASSWORD, password);

		// insert into database
		Long insertId;
		if (database.update(UserTable.TABLE_USER, values,
				UserTable.COLUMN_USERNAME, new String[] { username }) == 0) {
			insertId = database.insert(UserTable.TABLE_USER, null, values);
			this.close();
			return insertId;
		}

		this.close();
		return -1;
	}
	
	public void addUser(String firstName, String lastName, String username, String password)
	{
		if(userExists(username))
		{
			updateUser(firstName, lastName, username, password, username);
			return;
		}
//		if(getUserByUsername(username) == null)
//		{
//			return;
//		}
		ContentValues values = new ContentValues();
		values.put(UserTable.COLUMN_FIRST_NAME, firstName);
		values.put(UserTable.COLUMN_LAST_NAME, lastName);
		values.put(UserTable.COLUMN_USERNAME, username);
		values.put(UserTable.COLUMN_PASSWORD, password);
		this.open();
		database.insert(UserTable.TABLE_USER, null, values);
		this.close();		
	}

	public void updateUser(String firstName, String lastName, String username, String password, String oldUsername)
	{
		User userToUpdate = getUserByUsername(oldUsername);
		ContentValues values = new ContentValues();
		values.put(UserTable.COLUMN_FIRST_NAME, firstName);
		values.put(UserTable.COLUMN_LAST_NAME, lastName);
		values.put(UserTable.COLUMN_USERNAME, username);
		values.put(UserTable.COLUMN_PASSWORD, password);
		
		this.open();
		database.update(UserTable.TABLE_USER, values, UserTable.COLUMN_ID +" = ?", new String[]{String.valueOf(userToUpdate.get_id())});
		this.close();
	}

//	public User getUserByUsername(String username) {
//
//		Cursor cursor = database.query(UserTable.TABLE_USER, UserTable.COLUMNS,
//				UserTable.COLUMN_USERNAME + " = " + username , null, null,
//				null, null);
//		cursor.moveToFirst();
//		
//		// indexi su ovdije čisto iz razloga fleksibilnosti mijenjanja sheme baze
//		int index_id = cursor.getColumnIndex(UserTable.COLUMN_ID);
//		int indexFirstName = cursor.getColumnIndex(UserTable.COLUMN_FIRST_NAME);
//		int indexLastName = cursor.getColumnIndex(UserTable.COLUMN_LAST_NAME);
//		int indexUsername = cursor.getColumnIndex(UserTable.COLUMN_USERNAME);
//		int indexPassword = cursor.getColumnIndex(UserTable.COLUMN_PASSWORD);
//		
//		User user = new User(
//				cursor.getLong(index_id),
//				cursor.getString(indexFirstName),
//				cursor.getString(indexLastName),
//				cursor.getString(indexUsername),
//				cursor.getString(indexPassword)
//				);
//		cursor.close();
//		
//		return user;
//	}
	
	public User getUserByUsername(String username)
	{
		if(userExists(username))
		{
			this.open();		
			Cursor cursor = database.query(UserTable.TABLE_USER, UserTable.COLUMNS, UserTable.COLUMN_USERNAME+" = ?", new String[]{username}, null, null, null);
			if(cursor != null)
			{
				cursor.moveToFirst();
			}
//			if(cursor.getCount() == 0)
//			{
//				return null;
//			}
			User user = new User(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
			this.close();
			return user;		
		}
		return null;
	}
	
	public boolean userExists(String username)
	{
		this.open();		
		Cursor cursor = database.query(UserTable.TABLE_USER, UserTable.COLUMNS, UserTable.COLUMN_USERNAME+" = ?", new String[]{username}, null, null, null);
//		if(cursor != null)
//		{
//			cursor.moveToFirst();			
//		}
		if(cursor.getCount() == 0)
		{
			this.close();
			return false;
		}
		this.close();
		return true;
	}
	
	public List<User> getAllUsers()
	{
		List<User> users = new LinkedList<User>();
		String query = "SELECT * FROM " + UserTable.TABLE_USER;
		this.open();
		Cursor cursor = database.rawQuery(query, null);
		User user = null;
        if (cursor.moveToFirst()) {
            do {
            	user = new User(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));                               
                users.add(user);
            } while (cursor.moveToNext());
        }
        this.close();
		return users;
	}
	
	public void deleteUser(User user)
	{
		this.open();
		database.delete(UserTable.TABLE_USER, UserTable.COLUMN_ID+" = ?", new String[]{String.valueOf(user.get_id())});		
		this.close();
	}

}
