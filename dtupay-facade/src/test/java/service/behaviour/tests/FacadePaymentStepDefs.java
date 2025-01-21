package service.behaviour.tests;

import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.PaymentRequest;
import dtupay.services.facade.domain.models.Token;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public class FacadePaymentStepDefs {

	public class MockMessageQueue implements MessageQueue {

		private final Function<Event, String> idExtractor;

		public MockMessageQueue(Function<Event, String> mapping) {
			this.idExtractor = mapping;
		}

		@Override
		public void publish(Event event) {
			String id = idExtractor.apply(event);
			if (publishedEvents.containsKey(id)) {
				publishedEvents.get(id).complete(event);
			}
		}

		@Override
		public void addHandler(String topic, Consumer<Event> handler) {}
	}

	private Function<Event, String> paymentKeyExtractor = (event) ->
				 event.getArgument(0, PaymentRequest.class).token().toString();

	private MessageQueue paymentQ = new MockMessageQueue(paymentKeyExtractor);

	private Map<PaymentRequest, Correlator> payCorrelators = new ConcurrentHashMap<>();
	private PaymentRequest paymentRequest;
	private EventTypes eventTypeName;
	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();
	private Event mockEvent;
	private CompletableFuture<Boolean> futurePaymentSuccess = new CompletableFuture<>();

	private MerchantService payService = new MerchantService(paymentQ);

	@Given("a valid payment request")
	public void aValidPaymentRequest() {
		String merchantId = "123182752138-mid";
		UUID token = UUID.randomUUID();
		int amount = 50;
		paymentRequest = new PaymentRequest(merchantId, new Token(token), amount);

		mockEvent = new Event(EventTypes.PAYMENT_INITIATED.getTopic(), new Object[]{ paymentRequest });
		String id = paymentKeyExtractor.apply(mockEvent);

		publishedEvents.put(id, new CompletableFuture<>());
		assertNotNull(paymentRequest.merchantId());
		assertTrue(paymentRequest.amount() > 0);
	}

	@When("the payment request is initiated")
	public void aValidPaymentRequestIsInitiated() {
		new Thread(() -> {
			var success = payService.pay(paymentRequest);
			futurePaymentSuccess.complete(success);
		}).start();
	}

	@Then("the {string} event for the payment request is sent")
	public void theEventForThePaymentRequestIsSent(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		String id = paymentKeyExtractor.apply(mockEvent);

		Event event = publishedEvents.get(id).join();
		assertEquals(eventTypeName.getTopic(), event.getTopic());
		var payment = event.getArgument(0, PaymentRequest.class);
		var correlator = event.getArgument(1, Correlator.class);
		payCorrelators.put(payment, correlator);
	}

	@When("the {string} event is received")
	public void theEventIsReceived(String eventType) {
		eventTypeName = EventTypes.fromTopic(eventType);
		var correlator = payCorrelators.get(paymentRequest);
		assertNotNull(correlator);
		String placeholder = "placeholder";
		payService.handleBankTransferConfirmed(new Event(eventTypeName.getTopic(), new Object[] {
					placeholder, payCorrelators.get(paymentRequest)
		}));
	}

	@Then("the payment was successful")
	public void thePaymentWasSuccessful() {
		boolean success = futurePaymentSuccess.join();
		assertTrue(success);
	}
}
