package tests.dtupay;

import dtupay.model.Customer;
import dtupay.model.views.CustomerView;
import dtupay.services.CustomerService;
import dtupay.services.ReportService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

public class ReportingStepDefs {
	private CustomerService customerService = new CustomerService();
	private Customer registeredCustomer;
	private ReportService reportService = new ReportService();
	private ArrayList<CustomerView> responseCustomer;
	private Exception exception;

	@Given("a customer registered in DTUPay for reporting")
	public void a_customer_registered_in_DTUPay_for_reporting() {
		registeredCustomer = customerService.register(new Customer("John",
					"Sayna", "102030-1234", "9696", "rand"));
	}

	@When("the customer requests a report")
	public void the_customer_requests_a_report() {
		responseCustomer = reportService.getCustomerReport(registeredCustomer.payId());
	}

	@Then("the customer report is retrieved")
	public void the_customer_report_is_retrieved() {
		assertNotNull(responseCustomer);
	}
}
