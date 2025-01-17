package dtupay;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record Customer(String firstName, String lastName, String cpr, String bankAccountNo, String id) {

	// Secondary constructor with 'id' set to null by default
	public Customer(String firstName,
	                String lastName,
	                String cpr,
	                String bankAccountNo,
	                String id) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.cpr = cpr;
		this.bankAccountNo = bankAccountNo;
		this.id = id;
	}

}
