package hr.fer.zari.waspmote;

import java.io.Serializable;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public class mSensor implements Serializable
{
	private String _sensorName;
	private int _sensorType;
	private float _sensorValue = 0;
	//private Sensor _sensor;
	//ovo je potrebno zbog slanja preko Intenta!!!!!!! Inače baca Exception!!
	private transient Sensor _sensor;
	
	public mSensor(String name, int type, SensorManager _sManager)
	{
		_sensorName = name;
		_sensorType = type;		
		_sensor = _sManager.getDefaultSensor(type);		
	}
	
	public String getSensorName()
	{		
		return _sensorName;
	}
	
	public void setSensorName(String name)
	{
		_sensorName = name;
	}
	
	public int getSensorType()
	{
		return _sensorType;
	}
	
	public void setSensorType(int type)
	{
		_sensorType = type;
	}
	
	public float getSensorValue()
	{
		return _sensorValue;
	}
	
	public void setSensorValue(float value)
	{
		_sensorValue = value;
	}
	
	public Sensor getSensor()
	{
		return _sensor;
	}

}
