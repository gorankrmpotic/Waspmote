package hr.fer.zari.waspmote.models;

public class SensorMeasurement {
	
	private int _id;
	private int idSensor;
	private long timestamp;
	private String value;
	private String measurementUnit;
	
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public int getIdSensor() {
		return idSensor;
	}
	public void setIdSensor(int idSensor) {
		this.idSensor = idSensor;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getMeasurementUnit() {
		return measurementUnit;
	}
	public void setMeasurementUnit(String measurementUnit) {
		this.measurementUnit = measurementUnit;
	}
	
	public SensorMeasurement(int _id, int idSensor, long timestamp,
			String value, String measurementUnit) {
		super();
		this._id = _id;
		this.idSensor = idSensor;
		this.timestamp = timestamp;
		this.value = value;
		this.measurementUnit = measurementUnit;
	}
	
	

}
