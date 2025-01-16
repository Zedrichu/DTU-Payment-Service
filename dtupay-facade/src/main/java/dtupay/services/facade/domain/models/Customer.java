package dtupay.services.facade.domain.models;

import jakarta.json.bind.annotation.JsonbProperty;

public record Customer(String firstName, String lastName, String cpr, String bankAccountNo, String id) {

	// Secondary constructor with 'id' set to null by default
	public Customer(@JsonbProperty String firstName,
	                @JsonbProperty String lastName,
	                @JsonbProperty String cpr,
	                @JsonbProperty String bankAccountNo,
	                @JsonbProperty String id) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.cpr = cpr;
		this.bankAccountNo = bankAccountNo;
		this.id = id;
	}

}
