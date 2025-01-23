package tests.service.behaviours;

import dtupay.services.account.AccountManager;
import dtupay.services.account.domain.AccountRepository;
import dtupay.services.account.domain.MemoryAccountRepository;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.domain.models.Merchant;
import dtupay.services.account.domain.models.PaymentRequest;
import dtupay.services.account.domain.models.Token;
import dtupay.services.account.utilities.Correlator;
import dtupay.services.account.utilities.EventTypes;
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
    EventTypes eventTypeName;

    @Given("an unregistered customer")
    public void anUnregisteredCustomer() {

        customer = new Customer("Buyer", "Bajer", "12345", "deez", "nodder");
        customerId = "kajdfnga";
    }

    @Given("an unregistered merchant")
    public void anUnregisteredMerchant() {

        merchant = new Merchant("Seller", "Ja", "1234", "111", "asdfas");
        merchantId = "kjahsdflklja";
    }

    @Given("a registered merchant")
    public void aRegisteredMerchant() {
        merchant = new Merchant("Seller", "Ja", "1234", "111", "asdfas");
        merchantId = merchantAccountRepository.createAccount(merchant);
    }

    PaymentRequest paymentRequest;

    @When("the {string} event for the payment request is received")
    public void theEventForThePaymentRequestIsReceived(String arg0) {
        eventTypeName = EventTypes.fromTopic(arg0);
        paymentRequest = new PaymentRequest(merchantId, Token.random(), 100);
        correlator = Correlator.random();
        accountManagementService.handlePaymentInitiated(new Event(eventTypeName.getTopic(), new Object[] { paymentRequest, correlator }));
    }

    @Then("the {string} event is sent with the merchant information")
    public void theEventIsSentWithTheMerchantInformation(String arg0) {
        eventTypeName = EventTypes.fromTopic(arg0);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        receivedMerchant = receivedEvent.getArgument(0, Merchant.class);
        assertEquals(receivedEvent.getTopic(),eventTypeName.getTopic());
        assertEquals(correlator,receivedEvent.getArgument(1, Correlator.class));

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
    public void theEventForTheCustomerIdIsReceived(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        correlator = Correlator.random();
        if (eventTypeName.equals(EventTypes.TOKENS_REQUESTED)) {
            accountManagementService.handleTokensRequested(new Event(eventTypeName.getTopic(), new Object[] { customerId, 0, correlator }));
        } else {
            accountManagementService.handlePaymentTokenVerified(new Event(eventTypeName.getTopic(), new Object[]{ customerId, correlator }));
        }
    }

    @Then("the {string} event is sent with the customer information")
    public void theEventIsSentWithTheCustomerInformation(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        receivedCustomer = receivedEvent.getArgument(0, Customer.class);
        assertEquals(receivedEvent.getTopic(), eventTypeName.getTopic());
        assertEquals(correlator.getId(),receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @Then("the {string} event is sent with the error message {string}")
    public void theEventIsSentWithTheErrorMessage(String eventType, String errorMessage) {
        eventTypeName = EventTypes.fromTopic(eventType);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        String receivedErrorMessage = receivedEvent.getArgument(0, String.class);
        assertEquals(errorMessage,receivedErrorMessage);
        assertEquals(receivedEvent.getTopic(), eventTypeName.getTopic());
        assertEquals(correlator.getId(),receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @And("the customer account is verified")
    public void theCustomerAccountIsVerified() {
        assertEquals(customer.firstName(),receivedCustomer.firstName());
        assertEquals(customer.cpr(),receivedCustomer.cpr());
        assertEquals(customer.bankAccountNo(),receivedCustomer.bankAccountNo());
    }

    @Then("the {string} event is sent with no content and same correlation id")
    public void theEventIsSentWithNoContent(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        assertEquals(receivedEvent.getTopic(), eventTypeName.getTopic());
        assertEquals(correlator.getId(),receivedEvent.getArgument(0, Correlator.class).getId());
    }

    @When("the {string} event for unknown customer id is received")
    public void theEventForUnknownCustomerIdIsReceived(String arg0) {
        eventTypeName = EventTypes.fromTopic(arg0);
        correlator = Correlator.random();
        if (eventTypeName.equals(EventTypes.TOKENS_REQUESTED)) {
            accountManagementService.handleTokensRequested(new Event(eventTypeName.getTopic(), new Object[] { "<none>", 0, correlator }));
        } else {
            accountManagementService.handlePaymentTokenVerified(new Event(eventTypeName.getTopic(), new Object[]{ "<none>", correlator }));
        }
    }


}
