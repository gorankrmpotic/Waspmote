package hr.fer.zari.waspmote.models;

public class Subscription {
	
	private int _id;
	private int idGSN;
	private int period;
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public int get_idGSN() {
		return idGSN;
	}
	public void set_idGSN(int idGSN) {
		this.idGSN = idGSN;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	
	public Subscription(int _id, int idGSN, int period) {
		super();
		this._id = _id;
		this.idGSN = idGSN;
		this.period = period;
	}
	
	

}
