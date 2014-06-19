package hr.fer.zari.waspmote.models;

import java.io.Serializable;

public class Sensors implements Serializable {
	
	private int _id;
	private String sensorName;
	private int sensorType;
	
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getSensorName() {
		return sensorName;
	}
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}
	public int getSensorType() {
		return sensorType;
	}
	public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}
	public Sensors(int _id, String sensorName, int sensorType) {
		super();
		this._id = _id;
		this.sensorName = sensorName;
		this.sensorType = sensorType;
	}
	
	

}
