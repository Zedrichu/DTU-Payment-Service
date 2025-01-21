package service.behaviours.tests;

import com.google.gson.internal.LinkedTreeMap;
import dtupay.services.token.domain.models.*;
import dtupay.services.token.utilities.Correlator;
import dtupay.services.token.utilities.EventTypes;
import com.google.gson.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class TokenStepDefs {
  EventTypes eventType;
	MessageQueue messageQueue = mock(MessageQueue.class);
	Correlator correlator;
	String customerId;
	TokenRepository memoryTokenRepository = new MemoryTokenRepository();
	TokenManager tokenManager = new TokenManager(messageQueue, memoryTokenRepository);
	ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
	Token tokenUsed;
	ArrayList<Token> tokens;

	@Given("an existing customer with {int} tokens assigned")
	public void anExistingCustomerWithTokensAssigned(int noTokens) {
		customerId = "jeppe123";
		tokens = new ArrayList<>();

		for (int i = 0; i < noTokens; i++) {
			tokens.add(Token.random());
		}
		memoryTokenRepository.addTokens(customerId,tokens);
		tokenManager = new TokenManager(messageQueue, memoryTokenRepository);
	}


	@When("PaymentInitiated event is received for a payment request")
	public void paymentInitiatedEvent_is_received_for_a_payment_request() {
	  eventType = EventTypes.PAYMENT_INITIATED;
		correlator = Correlator.random();
		tokenUsed = tokens.getFirst();
		PaymentRequest paymentRequest = new PaymentRequest("jeppe", tokenUsed,1000 );
		tokenManager.handlePaymentInitiated(new Event(eventType.getTopic(),new Object[] {paymentRequest, correlator}));
	}

	@Then("PaymentTokenVerified event is sent for the payment with the customer id and the same correlation id")
	public void paymentTokenVerifiedEventIsSentForThePaymentWithTheCustomerIdAndSameCorrelationId() {
		eventType = EventTypes.PAYMENT_TOKEN_VERIFIED;
		eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(messageQueue).publish(eventCaptor.capture());
		Event receivedEvent = eventCaptor.getValue();

		String receivedCustomerId = receivedEvent.getArgument(0,String.class);
		Correlator receivedCorrelator = receivedEvent.getArgument(1,Correlator.class);


		assertEquals(customerId, receivedCustomerId);
		assertEquals(correlator, receivedCorrelator);
	}

	@Then("the token is no longer valid")
	public void theTokenIsNoLongerValid() {
		assertNull(memoryTokenRepository.extractId(tokenUsed));
	}

	@When("TokensRequested event is received for {int} tokens")
	public void tokensRequestedEventIsReceivedForTokens(int noTokens) {
		customerId = "newjeppe123";
		correlator = Correlator.random();
		eventType = EventTypes.TOKENS_REQUESTED;
		tokenManager.handleTokensRequested(new Event(eventType.getTopic(),new Object[] { customerId, noTokens, correlator}));
	}

	@When("TokenAccountVerified event is received for a customer")
	public void tokenAccountVerifiedEventIsReceivedForACustomer() {
		eventType = EventTypes.TOKEN_ACCOUNT_VERIFIED;
		tokenManager.handleTokenAccountVerified(new Event(eventType.getTopic(),new Object[] {correlator}));
	}

	ArrayList<Token> receivedTokenList;

	@Then("TokensGenerated event is sent with the same correlation id")
	public void tokensGeneratedEventIsSentWithTheSameCorrelationId() {
		eventType = EventTypes.TOKENS_GENERATED;
		eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(messageQueue).publish(eventCaptor.capture());
		Event receivedEvent = eventCaptor.getValue();

		ArrayList<LinkedTreeMap<String, Object>> list = receivedEvent.getArgument(0, ArrayList.class);
		Gson gson = new Gson();
		List<Token> tokenList = list.stream()
					.map(token -> gson.fromJson(gson.toJson(token), Token.class))
					.toList();
		receivedTokenList = new ArrayList<>(tokenList);
	}

	@And("{int} valid tokens are generated")
	public void validTokensAreGenerated(int noTokens) {
		assertEquals(noTokens, receivedTokenList.size());
		assertEquals(noTokens, receivedTokenList.stream().distinct().count());
	}

	@And("the customer has {int} valid tokens")
	public void theCustomerHasValidTokens(int noTokens) {
		assertEquals(noTokens, memoryTokenRepository.getNumberOfTokens(customerId));
	}

	@When("TokensRequested event is received for {int} tokens for the same customer id")
	public void tokensRequestedEventIsReceivedForTokensForTheSameCustomerId(int noTokens) {
		correlator = Correlator.random();
		eventType = EventTypes.TOKENS_REQUESTED;
		tokenManager.handleTokensRequested(new Event(eventType.getTopic(),new Object[] { customerId, noTokens, correlator}));

	}
	String receivedErrorMessage;

	@Then("TokensGenerationFailure event is sent with the same correlation id")
	public void tokensGenerationFailureEventIsSentWithTheSameCorrelationId() {
		eventType = EventTypes.TOKEN_GENERATION_FAILURE;
		eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(messageQueue).publish(eventCaptor.capture());
		Event receivedEvent = eventCaptor.getValue();
		assertEquals(eventType.getTopic(), receivedEvent.getTopic());
		assertEquals(correlator, receivedEvent.getArgument(1, Correlator.class));
		receivedErrorMessage = receivedEvent.getArgument(0, String.class);
	}

	@And("error message {string}")
	public void errorMessage(String expectedErrorMessage) {
		assertEquals(expectedErrorMessage,receivedErrorMessage);
	}

	@When("TokenAccountInvalid event is received for a customer")
	public void tokenAccountInvalidEventIsReceivedForACustomer() {
		eventType = EventTypes.TOKEN_ACCOUNT_INVALID;
		tokenManager.handleTokenAccountInvalid(new Event(eventType.getTopic(),new Object[] { correlator}));
	}
}