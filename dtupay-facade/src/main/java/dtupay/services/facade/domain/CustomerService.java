package dtupay.services.facade.domain;

import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.utilities.Correlator;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerService {

  private MessageQueue mque;
  private Map<Correlator, CompletableFuture<Customer>> correlations = new ConcurrentHashMap<>();

  public CustomerService(MessageQueue messageQueue) {

    this.mque = messageQueue;
    this.mque.addHandler("CustomerAccountCreated", this::handleCustomerAccountCreated);
  }

  public Customer register(Customer customer) {
    var correlationId = Correlator.random();
    correlations.put(correlationId, new CompletableFuture<>());
    Event event = new Event("CustomerRegistrationRequested", new Object[]{ customer, correlationId });
    mque.publish(event);
    return correlations.get(correlationId).join();
  }

  public void handleCustomerAccountCreated(Event e) {
    var s = e.getArgument(0, Customer.class);
    var correlationId = e.getArgument(1, Correlator.class);
    correlations.get(correlationId).complete(s);
  }

}
