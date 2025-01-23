package dtupay;

import dtupay.model.Customer;
import dtupay.services.CustomerService;
import dtupay.services.ReportService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class ReportingStepDefs {
	private CustomerService customerService = new CustomerService();
	private Customer registeredCustomer;
	private ReportService reportService = new ReportService();
	private String response;
	private Exception exception;

	@Given("a customer registered in DTUPay")
	public void a_customer_registered_in_DTUPay() {
		registeredCustomer = customerService.register(new Customer("John",
					"Sayna", "102030-1234", "9696", "rand"));
	}

	@When("the customer requests a report")
	public void the_customer_requests_a_report() {
		try {
			response = reportService.getCustomerReport(registeredCustomer.payId());
		} catch (Exception e) {
			exception = e;
		}
	}

	@Then("the customer report is retrieved")
	public void the_customer_report_is_retrieved() {

	}
}
