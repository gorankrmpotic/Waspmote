package hr.fer.zari.waspmote.models;

public class GSN {

	private int _id;
	private String ip;
	private String GSNName;
	private String GSNUsername;
	private String GSNPassword;
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getGSNName() {
		return GSNName;
	}
	public void setGSNName(String gSNName) {
		GSNName = gSNName;
	}
	public String getGSNUsername() {
		return GSNUsername;
	}
	public void setGSNUsername(String gSNUsername) {
		GSNUsername = gSNUsername;
	}
	public String getGSNPassword() {
		return GSNPassword;
	}
	public void setGSNPassword(String gSNPassword) {
		GSNPassword = gSNPassword;
	}
	public GSN(int _id, String ip, String gSNName, String gSNUsername,
			String gSNPassword) {
		super();
		this._id = _id;
		this.ip = ip;
		GSNName = gSNName;
		GSNUsername = gSNUsername;
		GSNPassword = gSNPassword;
	}
	
	
	
	
}
