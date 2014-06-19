package hr.fer.zari.waspmote.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Zadatak ovog servisa je očitavati podatke sa senzora po definiranim
 * kriterijima, te ih zapisivati u bazu. Ovaj servis jednom pokrenut ostaje
 * upogonjen neovisno o UI dretvi aplikacije.
 * 
 * @author Igor Petkovski
 * @version 1.0
 * 
 */
public class MeasurementService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
