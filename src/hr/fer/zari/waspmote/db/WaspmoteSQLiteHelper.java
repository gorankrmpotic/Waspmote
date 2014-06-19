package hr.fer.zari.waspmote.db;

import java.io.Serializable;

import hr.fer.zari.waspmote.db.dao.GSNDataSource;
import hr.fer.zari.waspmote.db.dao.ITableDataSource;
import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SensorSubscriptionDataSource;
import hr.fer.zari.waspmote.db.dao.SensorTypeDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.db.dao.SubscriptionDataSource;
//import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
//import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.db.dao.UserDataSource;
import hr.fer.zari.waspmote.db.tables.GSNTable;
import hr.fer.zari.waspmote.db.tables.SensorMeasurementTable;
import hr.fer.zari.waspmote.db.tables.SensorSubscriptionTable;
import hr.fer.zari.waspmote.db.tables.SensorTypeTable;
import hr.fer.zari.waspmote.db.tables.SensorsTable;
import hr.fer.zari.waspmote.db.tables.SubscriptionTable;
//import hr.fer.zari.waspmote.db.tables.SensorMeasurementTable;
//import hr.fer.zari.waspmote.db.tables.SensorsTable;
import hr.fer.zari.waspmote.db.tables.UserTable;
import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Zadaća ovog razreda je incijalno stvaranje baze, te adaptacija postojeće baze
 * prilikom mijenjanja sheme baze.
 * <p>
 * Prilikom stvaranja ili nadogradnje baze pozivaju se statičke metode pojedinih
 * tablica. Prilikom svake promjene sheme baze potrebno je inkrementirati
 * DATABASE_VERSION varijablu. Ukoliko se u bazu dodaje nova tablica u ovaj
 * razred potrebno je dodati samo pozive odgovorajućih metoda. Ukoliko se
 * mijenja pojedina tablica potrebno je promijeniti samo odgovarajuće metode u
 * razredu odgovarajuće tablice.
 * </p>
 * 
 * @author Igor Petkovski
 * @version 1.0
 * 
 */
public class WaspmoteSQLiteHelper extends SQLiteOpenHelper{


	private static final int DATABASE_VERSION = 13;
	private static final String DATABASE_NAME = "WaspmoteDB";

	public WaspmoteSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
//		db.execSQL(SensorsTable.CREATE_TABLE_SENSORS);
//		db.execSQL(SensorMeasurementTable.CREATE_TABLE_SENSOR_MEASUREMENT);		
		db.execSQL(UserTable.CREATE_TABLE_USER);
		db.execSQL(SensorTypeTable.CREATE_TABLE_SENSOR_TYPE);
		db.execSQL(GSNTable.CREATE_TABLE_GSN);
		db.execSQL(SubscriptionTable.CREATE_TABLE_SUBSCRIPTION);
		db.execSQL(SensorsTable.CREATE_TABLE_SENSORS);
		db.execSQL(SensorMeasurementTable.CREATE_TABLE_SENSOR_MEASUREMENTS);
		db.execSQL(SensorSubscriptionTable.CREATE_TABLE_SENSOR_SUBSCRIPTION);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+UserTable.TABLE_USER+" ;");	
		db.execSQL("DROP TABLE IF EXISTS "+SensorTypeTable.TABLE_SENSOR_TYPE+" ;");
		db.execSQL("DROP TABLE IF EXISTS "+GSNTable.TABLE_GSN+" ;");
		db.execSQL("DROP TABLE IF EXISTS "+SubscriptionTable.TABLE_SUBSCRIPTION+" ;");
		db.execSQL("DROP TABLE IF EXISTS "+SensorsTable.TABLE_SENSORS+" ;");
		db.execSQL("DROP TABLE IF EXISTS "+SensorMeasurementTable.TABLE_SENSOR_MEASUREMENT+" ;");
		db.execSQL("DROP TABLE IF EXISTS "+SensorSubscriptionTable.TABLE_SENSOR_SUBSCRIPTION+" ;");
		onCreate(db);
	}
	
	
	
	
	
//	public ITableDataSource getSensorsDAO(Context context) {
//		return new SensorsDataSource(context);
//	}
//	
//	public ITableDataSource getSensorMeasurementDataSource(Context context) {
//		return new SensorMeasurementDataSource(context);
//	}
	
	

	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
		db.setForeignKeyConstraintsEnabled(true);
	}

	public ITableDataSource getUserDataSource(Context context) {
		return new UserDataSource(context);
	}
	
	public ITableDataSource getSensorTypeDataSource(Context context){
		return new SensorTypeDataSource(context);
	}
	
	public ITableDataSource getSensorsDataSource(Context context){
		return new SensorsDataSource(context);
	}
	
	public ITableDataSource getGSNDataSource(Context context){
		return new GSNDataSource(context);
	}
	
	public ITableDataSource getSubscriptionDataSource(Context context){
		return new SubscriptionDataSource(context);
	}
	
	public ITableDataSource getSensorMeasurementDataSource(Context context){
		return new SensorMeasurementDataSource(context);
	}
	
	public ITableDataSource getSensorSubscriptionDataSource(Context context){
		return new SensorSubscriptionDataSource(context);
	}

}
