package dtupay.services.facade.domain.models;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record Customer(String firstName, String lastName, String cpr, String bankAccountNo, String payId) {

	/**
	 * Main constructor of Customer entity objects with JSON-B serialization tags.
	 * @param firstName
	 * @param lastName
	 * @param cpr
	 * @param bankAccountNo
	 * @param payId
	 */
	@JsonbCreator
	public Customer(@JsonbProperty String firstName,
	                @JsonbProperty String lastName,
	                @JsonbProperty String cpr,
	                @JsonbProperty String bankAccountNo,
	                @JsonbProperty String payId) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.cpr = cpr;
		this.bankAccountNo = bankAccountNo;
		this.payId = payId;
	}

}
