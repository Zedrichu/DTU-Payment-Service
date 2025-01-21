package service.behaviour.tests;

import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.PaymentRequest;
import dtupay.services.facade.domain.models.Token;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.exception.BankFailureException;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.Assert.*;

public class FacadePaymentStepDefs {

	private Function<Event, String> paymentKeyExtractor = (event) ->
				 event.getArgument(0, PaymentRequest.class).token().toString();

	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
	private MessageQueue paymentQ = new MockMessageQueue(paymentKeyExtractor, publishedEvents);

	private Map<PaymentRequest, Correlator> payCorrelators = new ConcurrentHashMap<>();
	private PaymentRequest paymentRequest;
	private CompletableFuture<Boolean> futurePaymentSuccess = new CompletableFuture<>();

	private MerchantService merchantService = new MerchantService(paymentQ);
	private Token token;
	Throwable exception;

	@Given("a payment request")
	public void aPaymentRequest() {
		String merchantId = "123182752138-mid";
		token = Token.random();
		int amount = 50;
		paymentRequest = new PaymentRequest(merchantId, token, amount);


		publishedEvents.put(token.toString(), new CompletableFuture<>());
		assertNotNull(paymentRequest.merchantId());
		assertTrue(paymentRequest.amount() > 0);
	}

	@When("the payment request is initiated")
	public void aValidPaymentRequestIsInitiated() {
		new Thread(() -> {
			try{
				var success = merchantService.pay(paymentRequest);
				futurePaymentSuccess.complete(success);
			}catch (CompletionException e){
				futurePaymentSuccess.completeExceptionally(e.getCause());
			}

		}).start();
	}

	@Then("the {string} event for the payment request is sent")
	public void theEventForThePaymentRequestIsSent(String eventType) {
		Event event = publishedEvents.get(token.toString()).join();
		assertEquals(eventType, event.getTopic());

		var payment = event.getArgument(0, PaymentRequest.class);
		var correlator = event.getArgument(1, Correlator.class);
		payCorrelators.put(payment, correlator);
	}

	@When("the {string} event is received")
	public void theEventIsReceived(String eventType) {
		var correlator = payCorrelators.get(paymentRequest);
		assertNotNull(correlator);

		merchantService.handleBankTransferConfirmed(new Event(eventType, new Object[] {
					"", payCorrelators.get(paymentRequest)
		}));
	}

	@Then("the payment was successful")
	public void thePaymentWasSuccessful() {
		boolean success = futurePaymentSuccess.join();
		assertTrue(success);
	}

	@When("the BankTransferConfirmed event is received for the same correlation id")
	public void theBankTransferConfirmedEventIsReceivedForTheSameCorrelationId() {
		var correlator = payCorrelators.get(paymentRequest);
		merchantService.handleBankTransferConfirmed(
					//TODO: add proper payment log
					new Event(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), "paylog",correlator));
	}

	@When("the BankTransferFailed event is received for the same correlation id")
	public void theBankTransferFailedEventIsReceivedForTheSameCorrelationId() {
		var correlator = payCorrelators.get(paymentRequest);
		merchantService.handleBankTransferFailed(
					new Event(EventTypes.BANK_TRANSFER_FAILED.getTopic(), "Bank transfer failed", correlator));
	}

	@Then("an BankFailure exception with message {string} is raised")
	public void anBankFailureExceptionWithMessageIsRaised(String message) {
		try {
			futurePaymentSuccess.join();
		} catch (CompletionException e) {
			exception = e.getCause();
		}
		assertNotNull(exception);
		assertTrue(exception instanceof BankFailureException);
		assertEquals(message, exception.getMessage());
	}
}
