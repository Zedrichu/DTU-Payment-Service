package dtupay.services.facade.domain;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dtupay.services.facade.annotations.MethodAuthor;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.models.Token;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.utilities.Correlator;
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

  public CustomerService(MessageQueue messageQueue) {
    logger.info("facade.CustomerService instantiated");
    this.mque = messageQueue;
    this.mque.addHandler("CustomerAccountCreated", this::handleCustomerAccountCreated);
    this.mque.addHandler("CustomerAccountCreationFailed", this::handleCustomerAccountCreationFailed);
    this.mque.addHandler("TokensGenerated", this::handleTokensGenerated);
  }

  public Customer register(Customer customer) throws CompletionException {
    logger.debug("Registration request for: {}", customer);
    var correlationId = Correlator.random();
    customerCorrelations.put(correlationId, new CompletableFuture<>());
    Event event = new Event("CustomerRegistrationRequested", new Object[]{ customer, correlationId });
    mque.publish(event);
    return customerCorrelations.get(correlationId).join();
  }

  public ArrayList<Token> requestTokens(int noTokens, String customerId) {
    logger.debug("Requesting tokens for: {}", customerId);
    var correlationId = Correlator.random();
    tokensCorrelations.put(correlationId, new CompletableFuture<>());

    Event event = new Event("TokensRequested", new Object[]{customerId, noTokens, correlationId});
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
    var noTokens = e.getArgument(1, Integer.class);
    var correlationId = e.getArgument(2, Correlator.class);
    Gson gson = new Gson();
    List<Token> tokenList = list.stream()
            .map(token -> gson.fromJson(gson.toJson(token), Token.class))
            .toList();
    ArrayList<Token> tokens = new ArrayList<Token>(tokenList);
    tokensCorrelations.get(correlationId).complete(tokens);
  }

  public void handleCustomerAccountCreationFailed(Event event) {
    logger.debug("Received CustomerAccountCreationFailed event: {}", event);
    var errorMessage = event.getArgument(0, String.class);
    var correlationId = event.getArgument(1, Correlator.class);
    customerCorrelations.get(correlationId).completeExceptionally(new AccountCreationException(errorMessage));
  }
}
