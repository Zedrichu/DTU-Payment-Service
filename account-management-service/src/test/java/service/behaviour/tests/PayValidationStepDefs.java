package service.behaviour.tests;

import dtupay.services.account.AccountManager;
import dtupay.services.account.domain.AccountRepository;
import dtupay.services.account.domain.MemoryAccountRepository;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.domain.models.Merchant;
import dtupay.services.account.domain.models.PaymentRequest;
import dtupay.services.account.utilities.Correlator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PayValidationStepDefs {
    MessageQueue queue = mock(MessageQueue.class);
    AccountRepository<Customer> customerAccountRepository = new MemoryAccountRepository<>();
    AccountRepository<Merchant> merchantAccountRepository = new MemoryAccountRepository<>();
    AccountManager accountManagementService = new AccountManager(queue, customerAccountRepository, merchantAccountRepository);
    Correlator correlator;
    ArgumentCaptor<Event> eventCaptor;
    Merchant merchant;
    Merchant receivedMerchant;
    Event receivedEvent;
    private String merchantId;
    Customer customer;
    Customer receivedCustomer;
    String customerId;

    @Given("a registered merchant")
    public void aRegisteredMerchant() {

        merchant = new Merchant("Seller", "Ja", "1234", "111", "asdfas");
        merchantId = merchantAccountRepository.createAccount(merchant);
    }

    PaymentRequest paymentRequest;

    @When("the {string} event for the payment request is received")
    public void theEventForThePaymentRequestIsReceived(String arg0) {
        paymentRequest = new PaymentRequest(merchantId, "token", 100);
        correlator = Correlator.random();
        accountManagementService.handlePaymentInitiated(new Event(arg0, new Object[] { paymentRequest, correlator }));
    }

    @Then("the {string} event is sent with the merchant information")
    public void theEventIsSentWithTheMerchantInformation(String arg0) {
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        receivedMerchant = receivedEvent.getArgument(0, Merchant.class);
        assertEquals(receivedEvent.getTopic(),arg0);
        assertEquals(correlator.getId(),receivedEvent.getArgument(1, Correlator.class).getId());

    }

    @And("the merchant account is verified")
    public void theMerchantAccountIsVerified() {
        assertEquals(merchant.firstName(),receivedMerchant.firstName());
        assertEquals(merchant.cpr(),receivedMerchant.cpr());
        assertEquals(merchant.bankAccountNo(),receivedMerchant.bankAccountNo());

    }

    @Given("a registered customer")
    public void aRegisteredCustomer() {
        customer = new Customer("Seller", "Ja", "1234", "111", "asdfas");
        customerId = customerAccountRepository.createAccount(customer);
    }

    @When("the {string} event for the customer id is received")
    public void theEventForTheCustomerIdIsReceived(String arg0) {
        correlator = Correlator.random();
        accountManagementService.handleTokenVerified(new Event(arg0, new Object[] { customerId, correlator }));

    }

    @Then("the {string} event is sent with the customer information")
    public void theEventIsSentWithTheCustomerInformation(String arg0) {
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        receivedCustomer = receivedEvent.getArgument(0, Customer.class);
        assertEquals(receivedEvent.getTopic(),arg0);
        assertEquals(correlator.getId(),receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @And("the customer account is verified")
    public void theCustomerAccountIsVerified() {
        assertEquals(customer.firstName(),receivedCustomer.firstName());
        assertEquals(customer.cpr(),receivedCustomer.cpr());
        assertEquals(customer.bankAccountNo(),receivedCustomer.bankAccountNo());
    }
}
