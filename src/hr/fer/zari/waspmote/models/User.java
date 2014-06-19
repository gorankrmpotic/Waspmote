package hr.fer.zari.waspmote.models;

public class User {

	private int _id;
	private String firstName;
	private String lastName;
	private String username;
	private String password;

	public int get_id() {
		return _id;
	}

	public User(int _id, String firstName, String lastName, String username,
			String password) {
		super();
		this._id = _id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
