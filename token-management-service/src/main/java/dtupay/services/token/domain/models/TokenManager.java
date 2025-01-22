package dtupay.services.token.domain.models;

import dtupay.services.token.annotations.MethodAuthor;

import dtupay.services.token.utilities.Correlator;
import dtupay.services.token.utilities.EventTypes;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    private MessageQueue mque;
    private TokenRepository repo;
    private Map<Correlator, TokenGenerationAggregate> aggregators = new ConcurrentHashMap<>();

    private static final int TOKEN_LIMIT = 5;

    public TokenManager(MessageQueue messageQueue, TokenRepository tokenRepository) {
        this.mque = messageQueue;
        this.repo = tokenRepository;

        this.mque.addHandler(EventTypes.TOKENS_REQUESTED.getTopic(), this::handleTokensRequested);
        this.mque.addHandler(EventTypes.TOKEN_ACCOUNT_VERIFIED.getTopic(), this::handleTokenAccountVerified);
        this.mque.addHandler(EventTypes.PAYMENT_INITIATED.getTopic(), this::handlePaymentInitiated);
        this.mque.addHandler(EventTypes.TOKEN_ACCOUNT_INVALID.getTopic(), this::handleTokenAccountInvalid );
        this.mque.addHandler(EventTypes.CUSTOMER_DEREGISTRATION_REQUESTED.getTopic(), this::handleCustomerDeregistrationRequested);
    }


    @MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
    public ArrayList<Token> generateTokens(int noTokens){
        ArrayList<Token> list = new ArrayList<>();
        for (int i = 0; i < noTokens; i++) {
            list.add(Token.random());
        }
        return list;
    }

    public synchronized TokenGenerationAggregate getOrCreateAggregate(Correlator correlator) {
        if (!aggregators.containsKey(correlator)) {
            aggregators.put(correlator,new TokenGenerationAggregate(correlator));
        }
        return aggregators.get(correlator);
    }

    public void acceptTokenGeneration(TokenGenerationAggregate aggregate){
        String customerId = aggregate.getCustomerId();
        int noTokens = aggregate.getNoToken();

        ArrayList<Token> tokenList = generateTokens(noTokens);
        repo.addTokens(customerId, tokenList);
        Event responseEvent = new Event(EventTypes.TOKENS_GENERATED.getTopic(),
              new Object[]{ tokenList, aggregate.getCorrelator()});
        mque.publish(responseEvent);
    }

    public void declineGeneration(TokenGenerationAggregate aggregate,String errorMessage) {
        Event responseEvent = new Event(EventTypes.TOKEN_GENERATION_FAILED.getTopic(),
              new Object[]{ errorMessage, aggregate.getCorrelator() });
        mque.publish(responseEvent);
    }

    public void completeGeneration(TokenGenerationAggregate aggregate) {
        if (aggregate.isComplete()) {
            if (!aggregate.isCustomerVerified()){
                declineGeneration(aggregate,"No tokens generated: Invalid customer id.");
            } else {
                int requestNoTokens = aggregate.getNoToken();
                int currentNoTokens = repo.getNumberOfTokens(aggregate.getCustomerId());

                if (currentNoTokens + requestNoTokens > TOKEN_LIMIT || requestNoTokens < 1) {
                    declineGeneration(aggregate,"No tokens generated: Invalid token amount.");
                } else {
                    acceptTokenGeneration(aggregate);
                }
            }
            aggregators.remove(aggregate.getCorrelator());
        }
    }

    public void handleTokensRequested(Event event){
        logger.debug("Received TokensRequest event: {}", event);

        Correlator correlator = event.getArgument(2, Correlator.class);
        int noTokens = event.getArgument(1, int.class);
        String customerId = event.getArgument(0, String.class);

        TokenGenerationAggregate aggregate = getOrCreateAggregate(correlator);
        aggregate.setNoToken(noTokens);
        aggregate.setCustomerId(customerId);
        aggregate.setRequestReceived(true);
        completeGeneration(aggregate);
    }

    public void handleTokenAccountVerified(Event event) {
        logger.debug("Received TokenAccountVerified event: {}", event);

        Correlator correlator = event.getArgument(0, Correlator.class);

        TokenGenerationAggregate aggregate = getOrCreateAggregate(correlator);
        aggregate.setCustomerVerified(true);
        aggregate.setCustomerHandled(true);
        completeGeneration(aggregate);
    }

    public void handlePaymentInitiated(Event event) {
        logger.debug("Received Payment Initiated event: {}", event);
        PaymentRequest paymentRequest = event.getArgument(0, PaymentRequest.class);
        Correlator correlator = event.getArgument(1, Correlator.class);
        Token token = paymentRequest.token();

        String customerId = repo.extractId(token);
        Event responseEvent;
        if (customerId == null) {
            responseEvent = new Event(EventTypes.PAYMENT_TOKEN_INVALID.getTopic(),new Object[]{"Invalid token.",correlator});
        }else{
            responseEvent = new Event(EventTypes.PAYMENT_TOKEN_VERIFIED.toString(), new Object[]{customerId ,correlator});
        }
        mque.publish(responseEvent);
    }

    public void handleTokenAccountInvalid(Event event) {
        logger.debug("Received TokenAccountInvalid event: {}", event);

        Correlator correlator = event.getArgument(0, Correlator.class);

        TokenGenerationAggregate aggregate = getOrCreateAggregate(correlator);
        aggregate.setCustomerVerified(false);
        aggregate.setCustomerHandled(true);
        completeGeneration(aggregate);
    }
    
    public void handleCustomerDeregistrationRequested(Event event) {
        logger.debug("Received CustomerDeregistrationRequested event: {}", event);
        String customerId = event.getArgument(0, String.class);
        Correlator correlator = event.getArgument(1, Correlator.class);

        Event responseEvent = new Event(EventTypes.CUSTOMER_TOKENS_DELETED.getTopic(), new Object[]{ correlator });
        if (repo.exists(customerId)) {
            repo.removeId(customerId);
        }
        mque.publish(responseEvent);
    }
}
