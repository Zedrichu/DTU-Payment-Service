package service.behaviours.tests;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dtupay.services.token.TokenManager;
import dtupay.services.token.models.Token;
import dtupay.services.token.utilities.Correlator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;

public class TokenStepDefs {
    MessageQueue messageQueue = mock(MessageQueue.class);
    TokenManager tokenManager = new TokenManager(messageQueue);
    ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
    Correlator correlator = Correlator.random();
    String customerId;
    int amount = 3;
    ArrayList<Token> generatedTokens;
    ArrayList<Token> receivedTokenList;

    @When("{string} event is received for a token request")
    public void eventIsReceivedForATokenRequest(String eventType) {
        customerId = "213014";
        generatedTokens = tokenManager.handleTokensRequested(new Event(eventType, new Object[]{ customerId, amount, correlator}));
    }

    @When("{string} event is received for a customer")
    public void eventIsReceivedForACustomer(String eventType) {
        tokenManager.handleTokenAccountVerified(new Event(eventType, new Object[]{ correlator }));
    }

    @Then("{string} event is sent with the same correlation id")
    public void eventIsSentWithTheSameCorrelationId(String eventType) {
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(messageQueue).publish(eventCaptor.capture());

        Event receivedEvent = eventCaptor.getValue();

        ArrayList<LinkedTreeMap<String, Object>> list = receivedEvent.getArgument(0, ArrayList.class);
        Gson gson = new Gson();
        List<Token> tokenList = list.stream()
                .map(token -> gson.fromJson(gson.toJson(token), Token.class))
                .toList();
        receivedTokenList = new ArrayList<>(tokenList);

        assertEquals(eventType, receivedEvent.getTopic());
        assertEquals(correlator.getId(), receivedEvent.getArgument(1, Correlator.class).getId());
    }

    @And("then {int} valid tokens are generated")
    public void thenValidTokensAreGenerated(int noTokens) {
        assertEquals(noTokens, receivedTokenList.size());
        assertEquals(noTokens, generatedTokens.size());
    }
}