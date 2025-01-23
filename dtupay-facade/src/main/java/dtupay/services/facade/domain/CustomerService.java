package dtupay.services.facade.domain;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dtupay.services.facade.annotations.MethodAuthor;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.models.Token;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.exception.AccountDeletionException;
import dtupay.services.facade.exception.InvalidAccountException;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerService {

  private Logger logger = LoggerFactory.getLogger(CustomerService.class);
  private MessageQueue mque;
  private Map<Correlator, CompletableFuture<Customer>> customerCorrelations = new ConcurrentHashMap<>();
  private Map<Correlator, CompletableFuture<ArrayList<Token>>> tokensCorrelations = new ConcurrentHashMap<>();
  private Map<Correlator, CompletableFuture<Boolean>> deregistrationCorrelations = new ConcurrentHashMap<>();
  private Map<Correlator, List<Event>> deregistrationEvents = new ConcurrentHashMap<>();

  public CustomerService(MessageQueue messageQueue) {
    logger.info("facade.CustomerService instantiated");

    this.mque = messageQueue;

    this.mque.addHandler(EventTypes.CUSTOMER_ACCOUNT_CREATED.getTopic(), this::handleCustomerAccountCreated);
    this.mque.addHandler(EventTypes.CUSTOMER_ACCOUNT_CREATION_FAILED.getTopic(), this::handleCustomerAccountCreationFailed);
    this.mque.addHandler(EventTypes.TOKENS_GENERATED.getTopic(), this::handleTokensGenerated);
    this.mque.addHandler(EventTypes.TOKEN_GENERATION_FAILED.getTopic(), this::handleTokenGenerationFailed);
    this.mque.addHandler(EventTypes.CUSTOMER_TOKENS_DELETED.getTopic(), this::handleCustomerDeregistered);
    this.mque.addHandler(EventTypes.CUSTOMER_DELETED.getTopic(), this::handleCustomerDeregistered);
    this.mque.addHandler(EventTypes.CUSTOMER_DELETE_FAILED.getTopic(),this::handleCustomerDeregistered);
  }

  public Customer register(Customer customer) throws CompletionException {
    logger.debug("Registration request for: {}", customer);
    var correlationId = Correlator.random();
    customerCorrelations.put(correlationId, new CompletableFuture<>());
    Event event = new Event(EventTypes.CUSTOMER_REGISTRATION_REQUESTED.getTopic(),
          new Object[]{ customer, correlationId });
    mque.publish(event);
    return customerCorrelations.get(correlationId).join();
  }

  public ArrayList<Token> requestTokens(int noTokens, String customerId) throws CompletionException {
    logger.debug("Requesting tokens for: {}", customerId);
    var correlationId = Correlator.random();
    tokensCorrelations.put(correlationId, new CompletableFuture<>());

    Event event = new Event(EventTypes.TOKENS_REQUESTED.getTopic(), new Object[]{customerId, noTokens, correlationId});
    mque.publish(event);
    return tokensCorrelations.get(correlationId).join();
  }

  public void handleCustomerAccountCreated(Event e) {
    logger.debug("Received CustomerAccountCreated event: {}: ", e);
    var reqCustomer = e.getArgument(0, Customer.class);
    var correlationId = e.getArgument(1, Correlator.class);
    customerCorrelations.get(correlationId).complete(reqCustomer);
  }

  @MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
  public void handleTokensGenerated(Event e) {
    logger.debug("Received TokensGenerated event: {}", e);
    ArrayList<LinkedTreeMap<String, Object>> list = e.getArgument(0, ArrayList.class);
    var correlationId = e.getArgument(1, Correlator.class);
    Gson gson = new Gson();
    List<Token> tokenList = list.stream()
            .map(token -> gson.fromJson(gson.toJson(token), Token.class))
            .toList();
    ArrayList<Token> tokens = new ArrayList<Token>(tokenList);
    tokensCorrelations.get(correlationId).complete(tokens);
  }

  public boolean deregister(String customerId) throws CompletionException {
     logger.debug("Deregistering customer with ID: {}", customerId);
     var correlationId = Correlator.random();
     deregistrationCorrelations.put(correlationId, new CompletableFuture<>());
     Event event = new Event(EventTypes.CUSTOMER_DEREGISTRATION_REQUESTED.getTopic(), new Object[]{ customerId, correlationId });
     mque.publish(event);
     return deregistrationCorrelations.get(correlationId).join();
  }

  public synchronized boolean logDeregistrationEventCheckCompletion(Event event) {
    var correlationId = event.getArgument(0, Correlator.class);
    if (deregistrationEvents.containsKey(correlationId)) {
      deregistrationEvents.get(correlationId).add(event);
    } else {
      deregistrationEvents.put(correlationId, new ArrayList<Event>());
      deregistrationEvents.get(correlationId).add(event);
    }
    return deregistrationEvents.get(correlationId).size() > 1;
  }

  public synchronized void handleCustomerDeregistered(Event event) {
    logger.debug("Received Customer Deregistered event: {}", event);
    var correlationId = event.getArgument(0, Correlator.class);
    if (logDeregistrationEventCheckCompletion(event)) {
      boolean deregisterSuccess = deregistrationEvents.get(correlationId).stream().allMatch(e -> e.getTopic().equals(EventTypes.CUSTOMER_DELETED.getTopic()) ||
              e.getTopic().equals(EventTypes.CUSTOMER_TOKENS_DELETED.getTopic()));
      if (deregisterSuccess) {
        deregistrationCorrelations.get(correlationId).complete(true);
      } else {
        deregistrationCorrelations.get(correlationId).completeExceptionally(new AccountDeletionException("Customer Deregistration Failed"));
      };
    }
  }


  public void handleCustomerAccountCreationFailed(Event event) {
    logger.debug("Received CustomerAccountCreationFailed event: {}", event);
    var errorMessage = event.getArgument(0, String.class);
    var correlationId = event.getArgument(1, Correlator.class);
    customerCorrelations.get(correlationId).completeExceptionally(new AccountCreationException(errorMessage));
  }

  public void handleTokenGenerationFailed(Event event) {
    logger.debug("Received TokensGenerationFailed event: {}", event);
    var errorMessage = event.getArgument(0, String.class);
    var correlationId = event.getArgument(1, Correlator.class);
    tokensCorrelations.get(correlationId).completeExceptionally(new InvalidAccountException(errorMessage));
  }
}
