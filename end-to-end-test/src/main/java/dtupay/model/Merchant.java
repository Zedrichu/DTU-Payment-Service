package dtupay.model;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record Merchant(String firstName, String lastName, String cpr, String bankAccountNo, String payId) {

	/**
	 * Main constructor of Merchant entity objects with JSON-B serialization tags for "value" properties
	 * @param firstName
	 * @param lastName
	 * @param cpr
	 * @param bankAccountNo
	 * @param payId
	 */
	@JsonbCreator
	public Merchant(@JsonbProperty String firstName,
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
