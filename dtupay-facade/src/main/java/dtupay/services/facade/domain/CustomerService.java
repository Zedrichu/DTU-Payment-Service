package dtupay.services.facade.domain;

import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.utilities.Correlator;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerService {

  private Logger logger = LoggerFactory.getLogger(CustomerService.class);
  private MessageQueue mque;
  private Map<Correlator, CompletableFuture<Customer>> correlations = new ConcurrentHashMap<>();

  public CustomerService(MessageQueue messageQueue) {
    logger.info("facade.CustomerService instantiated");
    this.mque = messageQueue;
    this.mque.addHandler("CustomerAccountCreated", this::handleCustomerAccountCreated);
    this.mque.addHandler("CustomerAccountCreationFailed", this::handleCustomerAccountCreationFailed);
  }

  public Customer register(Customer customer) throws CompletionException {
    logger.debug("Registration request for: {}", customer);
    var correlationId = Correlator.random();
    correlations.put(correlationId, new CompletableFuture<>());
    Event event = new Event("CustomerRegistrationRequested", new Object[]{ customer, correlationId });
    mque.publish(event);
    return correlations.get(correlationId).join();
  }

  public void handleCustomerAccountCreated(Event e) {
    logger.debug("Received CustomerAccountCreated event: {}", e);
    var s = e.getArgument(0, Customer.class);
    var correlationId = e.getArgument(1, Correlator.class);
    correlations.get(correlationId).complete(s);
  }

  public void handleCustomerAccountCreationFailed(Event event) {
    logger.debug("Received CustomerAccountCreationFailed event: {}", event);
    var errorMessage = event.getArgument(0, String.class);
    var correlationId = event.getArgument(1, Correlator.class);
    correlations.get(correlationId).completeExceptionally(new AccountCreationException(errorMessage));
  }
}
