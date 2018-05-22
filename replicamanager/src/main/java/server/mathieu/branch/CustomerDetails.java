package server.mathieu.branch;

public class CustomerDetails {
	private String firstName;
	private String lastName;
	private String address;
	private String phone;

	public CustomerDetails(String firstName, String lastName, String address, String phone) {
		if (lastName == null) {
			throw new IllegalArgumentException("The Customer must have a last name.");
		}
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.phone = phone;
	}

	public synchronized String getFirstName() {
		return firstName;
	}

	public synchronized void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public synchronized String getLastName() {
		return lastName;
	}

	public synchronized void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public synchronized String getAddress() {
		return address;
	}

	public synchronized void setAddress(String address) {
		this.address = address;
	}

	public synchronized String getPhone() {
		return phone;
	}

	public synchronized void setPhone(String phone) {
		this.phone = phone;
	}

}