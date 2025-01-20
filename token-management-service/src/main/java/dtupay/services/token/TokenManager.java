package dtupay.services.token;

import dtupay.services.token.annotations.MethodAuthor;
import dtupay.services.token.models.Token;
import dtupay.services.token.models.Customer;

import dtupay.services.token.utilities.Correlator;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    private MessageQueue mque;
    private HashMap<String, ArrayList<Token>> tokens = new HashMap<>();
    private HashMap<Correlator, String> tokensIdRequested = new HashMap<>();
    private HashMap<Correlator, Boolean> customerVerified = new HashMap<>();


    public TokenManager(MessageQueue messageQueue) {
        this.mque = messageQueue;

        this.mque.addHandler("TokensRequested", this::handleTokensRequested);
        this.mque.addHandler("TokenAccountVerified", this::handleTokenAccountVerified);
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
        }
    }

    public void completeGeneration(Correlator correlator){
        if (tokensIdRequested.containsKey(correlator) && customerVerified.containsKey(correlator)){
            ArrayList<Token> tokenList = tokens.get(tokensIdRequested.get(correlator));
            Event responseEvent = new Event("TokensGenerated", new Object[]{tokenList, correlator});
            mque.publish(responseEvent);
        }
    }

    public ArrayList<Token> handleTokensRequested(Event event){
        String customerId = event.getArgument(0,String.class);
        int noTokens = event.getArgument(1,int.class);
        Correlator correlator = event.getArgument(2, Correlator.class);
        generateTokens(customerId,noTokens);
        if(!tokensIdRequested.containsKey(correlator)){
            tokensIdRequested.put(correlator, customerId);
        }
        completeGeneration(correlator);
        return tokens.get(customerId);
    }

    public void handleTokenAccountVerified(Event event) {
        Correlator correlator = event.getArgument(0, Correlator.class);
        if(!customerVerified.containsKey(correlator)){
            customerVerified.put(correlator,true);
        }
        completeGeneration(correlator);
    }

    public void handlePaymentInitiated(Event event) {
    }


}
