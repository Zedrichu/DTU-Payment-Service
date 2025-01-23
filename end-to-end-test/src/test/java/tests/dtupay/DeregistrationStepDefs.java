package tests.dtupay;

import dtupay.exceptions.DeregisterException;
import dtupay.model.Customer;
import dtupay.model.Merchant;
import dtupay.services.CustomerService;
import dtupay.services.MerchantService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.junit.Assert.*;

public class DeregistrationStepDefs {
    private CustomerService customerService = new CustomerService();
    private Customer registeredCustomer;
    private MerchantService merchantService = new MerchantService();
    private Merchant registeredMerchant;
    private boolean response;
    private Exception exception;

    @Given("a customer registered in DTUPay")
    public void a_customer_registered_in_DTUPay() {
        registeredCustomer = customerService.register(new Customer("John",
                "Doe", "123456-1234", "1234", null));
    }

    @And("the customer has valid tokens")
    public void theCustomerHasValidTokens() {
        customerService.requestTokens(registeredCustomer.payId(),3);
    }
    @When("the customer is deregistered in DTUPay")
    public void the_customer_is_deregistered_in_DTUPay() {
        try {
            response = customerService.deregister(registeredCustomer.payId());
        } catch (Exception e) {
            exception = e;
        }
    }

    @Then("the customer is deregistered")
    public void theCustomerIsDeregistered() {
        assertTrue(response);
    }

    @Given("a merchant registered in DTUPay")
    public void aMerchantRegisteredInDTUPay() {
        registeredMerchant = merchantService.register(new Merchant("John", "Cena", "010203-1234", "000", null));
    }

    @When("the merchant is deregistered in DTUPay")
    public void theMerchantIsDeregisteredInDTUPay() {
        try {
            response = merchantService.deregister(registeredMerchant.payId());
        } catch (Exception e) {
            exception = e;
        }
    }



    @Then("the merchant is deregistered")
    public void theMerchantIsDeregistered() {
        assertTrue(response);
    }


    @When("the customer deregisters in DTUPay with the wrong ID")
    public void theCustomerDeregistersInDTUPayWithTheWrongID() {
        try {
            response = customerService.deregister("wrongid");
        } catch (Exception e) {
            exception = e;
        }
    }

    @When("the merchant deregisters in DTUPay with the wrong ID")
    public void theMerchantDeregistersInDTUPayWithTheWrongID() {
        try {
            response = merchantService.deregister("wrongid");
        } catch (Exception e) {
            exception = e;
        }
    }

    @Then("the customer receives an error message {string}")
    public void theCustomerReceivesAnErrorMessage(String errorMessage) {
        assertTrue(exception instanceof DeregisterException);
        assertEquals(errorMessage, exception.getMessage());
    }
    @Then("the merchant receives an error message {string}")
    public void theMerchantReceivesAnErrorMessage(String errorMessage) {
        assertTrue(exception instanceof DeregisterException);
        assertEquals(errorMessage, exception.getMessage());
    }
}
