package hr.fer.zari.waspmote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public class mSensors implements Serializable
{
private List<mSensor> listSensors = new ArrayList<mSensor>();
private static final long serialVersionUID = 1L;

	public mSensors(List<Sensor> sensors, SensorManager _sManager)
	{
		listSensors = new ArrayList<mSensor>();
		for(Sensor sens : sensors)
		{
			mSensor tmp = new mSensor(sens.getName(), sens.getType(), _sManager);
			listSensors.add(tmp);
		}
	}
	
	public mSensor getSensorByName(String name)
	{
		for(mSensor mSens : listSensors)
		{
			//if(mSens.getSensorName() == name)
			if(mSens.getSensorName().equals(name))
			{
				return mSens;
			}
		}
		return null;
	}
	
	public float getSensorValue(String name)
	{
		mSensor mSens = getSensorByName(name);
		if(mSens == null)
		{
			return 0;
		}
		return mSens.getSensorValue();
	}
	
	public List<mSensor> getAllSensors()
	{
		return listSensors;
	}
	
	public mSensor getSensorByType(int type)
	{
		for(mSensor mSens : listSensors)
		{
			if(mSens.getSensorType() == type)
			{
				return mSens;
			}
		}
		return null;
	}

}
