package service.behaviours.tests;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import dtupay.services.payment.PaymentManager;
import dtupay.services.payment.domain.models.Customer;
import dtupay.services.payment.domain.models.Merchant;
import dtupay.services.payment.domain.models.PaymentRequest;
import dtupay.services.payment.domain.models.Token;
import dtupay.services.payment.utilities.Correlator;
import dtupay.services.payment.utilities.EventTypes;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.After;
import messaging.Event;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PaymentExecutionStepDefs {

    MessageQueue queue = mock(MessageQueue.class);
    Correlator correlator = Correlator.random();
    PaymentRequest paymentRequest;
    Token token;
    PaymentManager paymentManager = new PaymentManager(queue);
    Customer customer;
    Merchant merchant;
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    BankService bankService = new BankServiceService().getBankServicePort();
    List<String> bankAccounts = new ArrayList<>();
    final int initialBalance = 1000;
    EventTypes eventTypeName;

    @When("the {string} event for a request is received")
    public void theEventForARequestIsReceived(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        token = Token.random();
        paymentRequest = new PaymentRequest("1231245", token,100);
        assertTrue(paymentRequest.amount() > 0);
        paymentManager.handlePaymentInitiated(new Event(eventTypeName.getTopic(),
                new Object[] { paymentRequest, correlator }));
    }

    @When("the {string} event for a customer is received")
    public void theEventForACustomerIsReceived(String eventType) throws BankServiceException_Exception {
        eventTypeName = EventTypes.fromTopic(eventType);
        User bankUser = new User();
        bankUser.setFirstName("John");
        bankUser.setLastName("Smith");
        bankUser.setCprNumber("020202-0202");
        String customerBankAccountNumber = bankService.createAccountWithBalance(bankUser, BigDecimal.valueOf(initialBalance));
        bankAccounts.add(customerBankAccountNumber);

        customer = new Customer(
                bankUser.getFirstName(), bankUser.getLastName(),
                bankUser.getCprNumber(), customerBankAccountNumber, "21312512");

        paymentManager.handleCustomerAccountVerified(new Event(eventTypeName.getTopic(), new Object[] { customer, correlator }));
    }

    @When("the {string} event for a merchant is received")
    public void theEventForAMerchantIsReceived(String eventType) throws BankServiceException_Exception {
        eventTypeName = EventTypes.fromTopic(eventType);
        User bankUser = new User();
        bankUser.setFirstName("Anne");
        bankUser.setLastName("Dove");
        bankUser.setCprNumber("050505-0202");
        String merchantBankAccountNumber = bankService.createAccountWithBalance(bankUser, BigDecimal.valueOf(initialBalance));
        bankAccounts.add(merchantBankAccountNumber);

        merchant = new Merchant(
                bankUser.getFirstName(), bankUser.getLastName(),
                bankUser.getCprNumber(), merchantBankAccountNumber, "21312512");

        paymentManager.handleMerchantAccountVerified(new Event(eventTypeName.getTopic(), new Object[] { merchant, correlator }));
    }

    private Event receivedEvent;

    @Then("the {string} event is sent with the same correlation id")
    public void theEventIsSentWithTheSameCorrelationId(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        assertEquals(eventTypeName.getTopic(), receivedEvent.getTopic());
        assertEquals(correlator.getId(),receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @After
    public void tearDown() throws BankServiceException_Exception {
        for (String bankAccount: bankAccounts) {
            bankService.retireAccount(bankAccount);
        }
    }

    @And("the amount in the payment request has been debited from the customer's bank account")
    public void theAmountInThePaymentRequestHasBeenDebitedFromTheCustomerSBankAccount() throws BankServiceException_Exception {
        var customerBalance = bankService.getAccount(customer.bankAccountNo()).getBalance();
        var merchantBalance = bankService.getAccount(merchant.bankAccountNo()).getBalance();

        assertEquals(0, customerBalance.compareTo(BigDecimal.valueOf(initialBalance - paymentRequest.amount())));
        assertEquals(0, merchantBalance.compareTo(BigDecimal.valueOf(initialBalance + paymentRequest.amount())));
    }

    @When("the {string} event for an error is received")
    public void theEventForAnErrorIsReceived(String errorEvent) {
        EventTypes eventType = EventTypes.fromTopic(errorEvent);

        if (eventType.equals(EventTypes.CUSTOMER_ACCOUNT_INVALID)) {
            paymentManager.handleCustomerAccountInvalid(new Event(EventTypes.CUSTOMER_ACCOUNT_INVALID.getTopic(), new Object[] { "invalid customer", correlator }));
        }
        if (eventType.equals(EventTypes.MERCHANT_ACCOUNT_INVALID)) {
            paymentManager.handleMerchantAccountInvalid(new Event(EventTypes.MERCHANT_ACCOUNT_INVALID.getTopic(), new Object[] { "invalid merchant", correlator }));
        }
        if (eventType.equals(EventTypes.PAYMENT_TOKEN_INVALID)){
            paymentManager.handlePaymentTokenInvalid((new Event(EventTypes.PAYMENT_TOKEN_INVALID.getTopic(), new Object[] { "Invalid token.", correlator })));
        }

    }
}

