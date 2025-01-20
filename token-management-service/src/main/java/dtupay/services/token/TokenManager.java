package dtupay.services.token;

import dtupay.services.token.annotations.MethodAuthor;
import dtupay.services.token.models.PaymentRequest;
import dtupay.services.token.models.Token;

import dtupay.services.token.utilities.Correlator;
import lombok.Setter;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    private MessageQueue mque;

    private HashMap<String, ArrayList<Token>> tokens = new HashMap<>();

    private HashMap<Correlator, Event> tokensRequested = new HashMap<>();
    private HashMap<Correlator, Boolean> customerVerified = new HashMap<>();


    public TokenManager(MessageQueue messageQueue) {
        this.mque = messageQueue;

        this.mque.addHandler("TokensRequested", this::handleTokensRequested);
        this.mque.addHandler("TokenAccountVerified", this::handleTokenAccountVerified);
        this.mque.addHandler("PaymentInitiated", this::handleTokenAccountVerified);
    }

    public void setTokens(HashMap<String, ArrayList<Token>> tokens) {
        this.tokens = tokens;
    }
    public int getAmountOfTokens(String customerId) {
        if (tokens.containsKey(customerId)) {
            return tokens.get(customerId).size();
        }
        return 0;
    }

    @MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
    public void generateTokens(String customerId, int noTokens){
        if (!tokens.containsKey(customerId)){
            ArrayList<Token> tokenList = new ArrayList<>();
            // TODO: Less than <= 5 Tokens maximum
            for(int i=0; i<noTokens; i++){
                tokenList.add(Token.random());
            }
            tokens.put(customerId, tokenList);
        } else {
            ArrayList<Token> tokenList = tokens.get(customerId);
            //should we say the
            // TODO: Create else for statement tokens > 1
            // TODO: Less than <= 5 Tokens maximum
            if (tokenList.size()<=1){
                for(int i=0; i<noTokens; i++){
                    tokenList.add(Token.random());
                }
                tokens.replace(customerId,tokenList);
            }
        }
    }

    public void completeGeneration(Correlator correlator){
        if (tokensRequested.containsKey(correlator) && customerVerified.containsKey(correlator)){
            String customerId = tokensRequested.get(correlator).getArgument(0,String.class);
            int noTokens = tokensRequested.get(correlator).getArgument(1,int.class);
            generateTokens(customerId,noTokens);
            ArrayList<Token> tokenList = tokens.get(customerId);
            Event responseEvent = new Event("TokensGenerated", new Object[]{tokenList, correlator});
            mque.publish(responseEvent);
        }
    }

    public void handleTokensRequested(Event event){
        logger.debug("Received TokensRequest event: {}", event);
        Correlator correlator = event.getArgument(2, Correlator.class);
        if(!tokensRequested.containsKey(correlator)){
            tokensRequested.put(correlator, event);
        }
        completeGeneration(correlator);
    }

    public void handleTokenAccountVerified(Event event) {
        logger.debug("Received TokenAccountVerified event: {}", event);
        Correlator correlator = event.getArgument(0, Correlator.class);
        if(!customerVerified.containsKey(correlator)){
            customerVerified.put(correlator,true);
        }
        completeGeneration(correlator);
    }

    public ArrayList<Token> handlePaymentInitiated(Event event) {
        logger.debug("Received PaymentInitiated event: {}", event);
        PaymentRequest paymentRequest = event.getArgument(0, PaymentRequest.class);
        Correlator correlator = event.getArgument(1, Correlator.class);
        Token token = paymentRequest.token();
        for (Map.Entry<String,ArrayList<Token>> entry: tokens.entrySet()){
            // TODO: implement failure for token does not exist
            if(entry.getValue().contains(token)){
                String customerId = entry.getKey();
                ArrayList<Token> retrievedTokens = entry.getValue();
                retrievedTokens.remove(token);
                Event responseEvent = new Event("PaymentTokenVerified", new Object[]{customerId ,correlator});
                mque.publish(responseEvent);
                return tokens.replace(customerId,retrievedTokens);
            }
        }
        return null;
    }


}
