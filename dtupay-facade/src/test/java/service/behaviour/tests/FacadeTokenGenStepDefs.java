package service.behaviour.tests;

import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.models.Token;
import dtupay.services.facade.exception.InvalidAccountException;
import dtupay.services.facade.utilities.Correlator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.Assert.*;

public class FacadeTokenGenStepDefs {

	private Map<String, CompletableFuture<Event>> publishedEvents = new ConcurrentHashMap<>();

	private final Function<Event, String> tokenKeyExtractor =
				(event) -> event.getArgument(0, String.class);


	private final MessageQueue tokensQ = new MockMessageQueue(tokenKeyExtractor, publishedEvents);

	private CustomerService tokenService = new CustomerService(tokensQ);

	private Map<String, Correlator> tCorrelators = new ConcurrentHashMap<>();

	private CompletableFuture<ArrayList<Token>> futureTokenRequest = new CompletableFuture<>();
	private CompletableFuture<ArrayList<Token>> secondFutureTokenRequest = new CompletableFuture<>();

	private String regCustomerId;
	private String secondRegCustomerId;

	private Throwable exception;


	@Given("a registered customer")
	public void aRegisteredCustomerWithTokens() {
		regCustomerId = "<existent-id>";
		publishedEvents.put(regCustomerId, new CompletableFuture<>());
	}

	@Given("a second registered customer")
	public void aSecondRegisteredCustomerWithTokens() {
		secondRegCustomerId = "<second-id>";
		publishedEvents.put(secondRegCustomerId, new CompletableFuture<>());
	}

	@When("the customer requests {int} tokens")
	public void theCustomerRequestsTokens(int noTokens) {
		new Thread(() -> {
			try {
				var tokenList = tokenService.requestTokens(noTokens, regCustomerId);
				futureTokenRequest.complete(tokenList);
			} catch (CompletionException e) {
				futureTokenRequest.completeExceptionally(e.getCause());
			}
		}).start();
	}

	@When("the second customer requests {int} tokens")
	public void theSecondCustomerRequestsTokens(int noTokens) {
		new Thread(() -> {
			try {
				var tokenList = tokenService.requestTokens(noTokens, secondRegCustomerId);
				secondFutureTokenRequest.complete(tokenList);
			} catch (CompletionException e) {
				secondFutureTokenRequest.completeExceptionally(e.getCause());
			}
		}).start();
	}

	@Then("the {string} event is sent asking {int} tokens for that customer id")
	public void theEventIsSentWithTokensForThatCustomerId(String eventType, int noTokens) {
		Event event = publishedEvents.get(regCustomerId).join();
		assertEquals(eventType, event.getTopic());

		var customerId = event.getArgument(0, String.class);
		var tokens = event.getArgument(1, Integer.class);
		var correlator = event.getArgument(2, Correlator.class);

		assertEquals(noTokens, tokens.intValue());
		tCorrelators.put(customerId, correlator);
	}

	@Then("the {string} event is sent asking {int} tokens for the second customer id")
	public void theEventIsSentAskingTokensForTheSecondCustomerId(String eventType, int noTokens) {
		Event event = publishedEvents.get(secondRegCustomerId).join();
		assertEquals(eventType, event.getTopic());

		var customerId = event.getArgument(0, String.class);
		var tokens = event.getArgument(1, Integer.class);
		var correlator = event.getArgument(2, Correlator.class);

		assertEquals(noTokens, tokens.intValue());
		tCorrelators.put(customerId, correlator);
	}

	@When("the {string} event is received for the same customer with {int} tokens")
	public void theEventIsReceivedForTheSameCustomerWithTokens(String eventType, int noTokens) {
		var correlator = tCorrelators.get(regCustomerId);
		ArrayList<Token> tokenList = new ArrayList<>();
		for (int i=0; i < noTokens; i++) {
			tokenList.add(Token.random());
		}
		assertNotNull(correlator);
		tokenService.handleTokensGenerated(new Event(eventType, tokenList, correlator));
	}

	@When("the {string} event is received for the second customer with {int} tokens")
	public void theEventIsReceivedForTheSecondCustomerWithTokens(String eventType, int noTokens) {
		var correlator = tCorrelators.get(secondRegCustomerId);
		ArrayList<Token> tokenList = new ArrayList<>();
		for (int i=0; i < noTokens; i++) {
			tokenList.add(Token.random());
		}
		assertNotNull(correlator);
		tokenService.handleTokensGenerated(new Event(eventType, tokenList, correlator));
	}

	@Then("the customer has {int} new valid tokens")
	public void theCustomerHasValidTokens(int noTokens) {
		var tokenList = futureTokenRequest.join();
		assertEquals(noTokens, tokenList.size());
		assertEquals(noTokens, tokenList.stream().distinct().count());
	}

	@Then("the second customer has {int} new valid tokens")
	public void theSecondCustomerHasValidTokens(int noTokens) {
		var tokenList = secondFutureTokenRequest.join();
		assertEquals(noTokens, tokenList.size());
		assertEquals(noTokens, tokenList.stream().distinct().count());
	}

	@Given("an unregistered customer id")
	public void anUnregisteredCustomerId() {
		regCustomerId = "<none>";
		publishedEvents.put(regCustomerId, new CompletableFuture<>());
	}

	@When("the {string} event is received for the same correlation id")
	public void theEventIsReceivedForTheSameCorrelationId(String eventType) {
		var correlator = tCorrelators.get(regCustomerId);
		tokenService.handleTokenGenerationFailure(new Event(eventType,
				new Object[] {"No tokens generated: Invalid customer id.", correlator}));
	}

	@Then("an InvalidAccount exception with message {string} is raised")
	public void anExceptionWithMessageIsRaised(String errorMessage) {
		try {
			futureTokenRequest.join();
		} catch (CompletionException e) {
			exception = e.getCause();
		}
		assertNotNull(exception);
		assertTrue(exception instanceof InvalidAccountException);
		assertEquals(errorMessage, exception.getMessage());
	}

}
