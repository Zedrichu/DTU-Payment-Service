package dtupay.facade.domain;

import dtupay.facade.domain.models.Customer;
import messaging.Event;
import messaging.MessageQueue;
import java.util.concurrent.CompletableFuture;

public class CustomerService {

  private MessageQueue mque;
  private CompletableFuture<String> registeredCustomerId;

  public CustomerService(MessageQueue messageQueue) {

    this.mque = messageQueue;
    this.mque.addHandler("CustomerRegistered", this::handleCustomerRegistered);
  }

  public String register(Customer customer) {
    registeredCustomerId = new CompletableFuture<>();
    Event event = new Event("CustomerRegistrationRequested", new Object[]{ customer });
    mque.publish(event);
    System.out.println("Event published!");
    return registeredCustomerId.join();
  }

  public void handleCustomerRegistered(Event e) {
    var s = e.getArgument(0, String.class);
    registeredCustomerId.complete(s);
  }



}
