package hr.fer.zari.waspmote.models;

public class SensorSubscription {
	
	private int _idSubscription;
	private int _idSensor;
	
	public int get_idSubscription() {
		return _idSubscription;
	}
	public void set_idSubscription(int _idSubscription) {
		this._idSubscription = _idSubscription;
	}
	public int get_idSensor() {
		return _idSensor;
	}
	public void set_idSensor(int _idSensor) {
		this._idSensor = _idSensor;
	}
	
	public SensorSubscription(int _idSubscription, int _idSensor) {
		super();
		this._idSubscription = _idSubscription;
		this._idSensor = _idSensor;
	}
	
	

}
