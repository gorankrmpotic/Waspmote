package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.models.Sensors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServiceData implements Serializable {
	
	private List<Sensors> sensors;
	private int period;	
	private String gsnIp;
	private String gsnUsername;
	private String gsnPassword;	
	
	public List<Sensors> getSensors() {
		return sensors;
	}
	public void setSensors(List<Sensors> sensors) {
		this.sensors = sensors;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	public ServiceData(List<Sensors> sensors, int period) {
		super();
		this.sensors = sensors;
		this.period = period;
	}
	
	public List<Sensors> getExternalBluetoothSensors()
	{
		List<Sensors> extBluetooth = new ArrayList<Sensors>();
		for(Sensors sens : sensors)
		{
			if(sens.getSensorType() == 2)
			{
				extBluetooth.add(sens);
			}
		}
		return extBluetooth;
	}
	
	public boolean containsExternalBluetoothSensors()
	{
		for(Sensors sens : sensors)
		{
			if(sens.getSensorType() == 2)
			{
				return true;
			}
		}
		return false;
	}
	
	public List<Sensors> getInternalSensors()
	{
		List<Sensors> intSens = new ArrayList<Sensors>();
		for(Sensors sens : sensors)
		{
			if(sens.getSensorType() == 1)
			{
				intSens.add(sens);
			}
		}
		return intSens;
	}
	
	public boolean containsInternalSensors()
	{
		for(Sensors sens : sensors)
		{
			if(sens.getSensorType() == 1)
			{
				return true;
			}
		}
		return false;
	}
	
	public int getSensorIdByName(String sensorName)
	{
		for(Sensors sens : sensors)
		{
			if(sens.getSensorName().equals(sensorName))
			{
				return sens.get_id();
			}
		}
		return -1;
	}
	
	public Sensors getSensorById(int id)
	{
		for(Sensors sens : sensors)
		{
			if(sens.get_id() == id)
			{
				return sens;
			}
		}
		return null;
	}
	
	public String getGsnIp() {
		return gsnIp;
	}
	public void setGsnIp(String gsnIp) {
		this.gsnIp = gsnIp;
	}
	public String getGsnUsername() {
		return gsnUsername;
	}
	public void setGsnUsername(String gsnUsername) {
		this.gsnUsername = gsnUsername;
	}
	public String getGsnPassword() {
		return gsnPassword;
	}
	public void setGsnPassword(String gsnPassword) {
		this.gsnPassword = gsnPassword;
	}
	
	public ServiceData(List<Sensors> sensors, int period, String gsnIp,
			String gsnUsername, String gsnPassword) {
		super();
		this.sensors = sensors;
		this.period = period;
		this.gsnIp = gsnIp;
		this.gsnUsername = gsnUsername;
		this.gsnPassword = gsnPassword;
	}
	
	

}
