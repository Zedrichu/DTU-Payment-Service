package dtupay;

import dtupay.model.Customer;
import dtupay.services.CustomerService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.Assert.*;

public class DeregistrationStepDefs {
	private CustomerService customerService = new CustomerService();
	private Customer registeredCustomer;
	private String response;
	private Exception exception;

	@Given("a customer registered in DTUPay")
	public void a_customer_registered_in_DTUPay() {
			registeredCustomer = customerService.register(new Customer("John",
						"Doe", "123456-1234", "1234", "regId123"));
	}

	@When("the customer is deregistered in DTUPay")
	public void the_customer_is_deregistered_in_DTUPay() {
		try {
			response = customerService.deregister(registeredCustomer.payId());
		} catch (Exception e){
				exception = e;
		}
	}

	@Then("the customer receives a confirmation message {}")
	public void the_customer_receives_a_event(String message) {
		assertEquals(message, response);
	}

}
