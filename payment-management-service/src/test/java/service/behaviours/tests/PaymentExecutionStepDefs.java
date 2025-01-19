package service.behaviours.tests;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.User;
import dtupay.services.payment.PaymentManager;
import dtupay.services.payment.domain.models.BankTransferAggregator;
import dtupay.services.payment.domain.models.Customer;
import dtupay.services.payment.domain.models.Merchant;
import dtupay.services.payment.domain.models.PaymentRequest;
import dtupay.services.payment.utilities.Correlator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.junit.After;
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
    String token;
    PaymentManager paymentManager = new PaymentManager(queue);
    Customer customer;
    Merchant merchant;
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    BankService bankService = new BankServiceService().getBankServicePort();
    List<String> bankAccounts = new ArrayList<>();

    @When("the {string} event for a request is received")
    public void theEventForARequestIsReceived(String eventType) {
        token = "token";
        paymentRequest = new PaymentRequest("1231245", "token",100);
        assertTrue(paymentRequest.amount() > 0);
        paymentManager.handlePaymentInitiated(new Event(eventType,
                new Object[] { paymentRequest, correlator }));
    }

    @When("the {string} event for a customer is received")
    public void theEventForACustomerIsReceived(String eventType) throws BankServiceException_Exception {
        User bankUser = new User();
        bankUser.setFirstName("John");
        bankUser.setLastName("Smith");
        bankUser.setCprNumber("020202-0202");
        String customerBankAccountNumber = bankService.createAccountWithBalance(bankUser, BigDecimal.valueOf(1000));
        bankAccounts.add(customerBankAccountNumber);

        customer = new Customer(
                bankUser.getFirstName(), bankUser.getLastName(),
                bankUser.getCprNumber(), customerBankAccountNumber, "21312512");

        paymentManager.handleCustomerAccountVerified(new Event(eventType, new Object[] { customer, correlator }));
    }

    @When("the {string} event for a merchant is received")
    public void theEventForAMerchantIsReceived(String eventType) throws BankServiceException_Exception {
        User bankUser = new User();
        bankUser.setFirstName("Anne");
        bankUser.setLastName("Dove");
        bankUser.setCprNumber("050505-0202");
        String merchantBankAccountNumber = bankService.createAccountWithBalance(bankUser, BigDecimal.valueOf(1000));
        bankAccounts.add(merchantBankAccountNumber);

        merchant = new Merchant(
                bankUser.getFirstName(), bankUser.getLastName(),
                bankUser.getCprNumber(), merchantBankAccountNumber, "21312512");

        paymentManager.handleMerchantAccountVerified(new Event(eventType, new Object[] { merchant, correlator }));
    }

    private Event receivedEvent;

    @Then("the {string} event is sent with the same correlation id")
    public void theEventIsSentWithTheSameCorrelationId(String eventType) {
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        assertEquals(eventType, receivedEvent.getTopic());
        assertEquals(correlator.getId(),receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @After
    public void tearDown() throws BankServiceException_Exception {
        for (String bankAccount: bankAccounts) {
            bankService.retireAccount(bankAccount);
        }
    }
}
