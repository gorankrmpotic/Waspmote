package hr.fer.zari.waspmote.models;

public class SensorType {	
	
	private int _id;
	private String sensorType;
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getSensorType() {
		return sensorType;
	}
	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}
	
	public SensorType(int _id, String sensorType) {
		super();
		this._id = _id;
		this.sensorType = sensorType;
	}
	
}
