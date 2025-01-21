package service.behaviours.tests;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dtupay.services.token.TokenManager;
import dtupay.services.token.models.PaymentRequest;
import dtupay.services.token.models.Token;
import dtupay.services.token.utilities.Correlator;
import dtupay.services.token.utilities.EventTypes;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TokenStepDefs {
    MessageQueue messageQueue = mock(MessageQueue.class);
    TokenManager tokenManager = new TokenManager(messageQueue);
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    Correlator correlator = Correlator.random();
    String customerId;
    int amount = 3; //TODO: Should not be hard coded but in feature file maybe
    ArrayList<Token> receivedTokenList;
    EventTypes eventTypeName;

    @When("{string} event is received for a token request")
    public void eventIsReceivedForATokenRequest(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        customerId = "213014";
        tokenManager.handleTokensRequested(new Event(eventTypeName.getTopic(), new Object[]{ customerId, amount, correlator}));
    }

    @When("{string} event is received for a customer")
    public void eventIsReceivedForACustomer(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        tokenManager.handleTokenAccountVerified(new Event(eventTypeName.getTopic(), new Object[]{ correlator}));
    }

    @Then("{string} event is sent with the same correlation id")
    public void eventIsSentWithTheSameCorrelationId(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(messageQueue).publish(eventCaptor.capture());

        Event receivedEvent = eventCaptor.getValue();

        ArrayList<LinkedTreeMap<String, Object>> list = receivedEvent.getArgument(0, ArrayList.class);
        Gson gson = new Gson();
        List<Token> tokenList = list.stream()
                .map(token -> gson.fromJson(gson.toJson(token), Token.class))
                .toList();
        receivedTokenList = new ArrayList<>(tokenList);

        assertEquals(eventTypeName.getTopic(), receivedEvent.getTopic());
        assertEquals(correlator.getId(), receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @And("then {int} valid tokens are generated")
    public void thenValidTokensAreGenerated(int noTokens) {
        assertEquals(noTokens, receivedTokenList.size());
    }

    private PaymentRequest paymentRequest;
    private Token token;
    private Correlator correlator2 = Correlator.random();
    private ArrayList<Token> tokensLeft;
    private Event receivedEvent;

    @When("{string} event is received for a payment request")
    public void eventIsReceivedForAPaymentRequest(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        token = Token.random();
        customerId = "111111111";
        ArrayList<Token> tempTokens = new ArrayList<>(Arrays.asList(token));
        HashMap<String, ArrayList<Token>> tokens = new HashMap<>(){{put(customerId,tempTokens);}};
        tokenManager.setTokens(tokens);
        paymentRequest = new PaymentRequest("12312341", token, 100);
        tokensLeft = tokenManager.handlePaymentInitiated(new Event(eventTypeName.getTopic(), new Object[]{paymentRequest, correlator2}));
        assertFalse(tokensLeft.contains(token));
    }

    @Then("{string} is sent with the same correlation id")
    public void isSentWithTheSameCorrelationId(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(messageQueue).publish(eventCaptor.capture());
        receivedEvent = eventCaptor.getValue();
        assertEquals(eventTypeName.getTopic(), receivedEvent.getTopic());
        assertEquals(correlator2.getId(),receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @When("{string} event is received for a token request for existing customer")
    public void eventIsReceivedForATokenRequestForExistingCustomer(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        token = Token.random();
        customerId = "121212121";
        ArrayList<Token> tempTokens = new ArrayList<>(Arrays.asList(token));
        HashMap<String, ArrayList<Token>> tokens = new HashMap<>(){{put(customerId,tempTokens);}};
        tokenManager.setTokens(tokens);


        tokenManager.handleTokensRequested(new Event(eventTypeName.getTopic(), new Object[]{ customerId, amount, correlator}));


    }

    @When("{string} event is received for a token request for new customer")
    public void eventIsReceivedForATokenRequestForNewCustomer(String eventType) {
        eventTypeName = EventTypes.fromTopic(eventType);
        customerId = "121212121";
        tokenManager.handleTokensRequested(new Event(eventTypeName.getTopic(), new Object[]{ customerId, amount, correlator}));
    }
}