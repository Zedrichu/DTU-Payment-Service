package course.webservicedev.domain;

import course.webservicedev.domain.models.Customer;
import messaging.MessageQueue;

public class CustomerService {

  private MessageQueue mq;

  public CustomerService(MessageQueue messageQueue) {
    this.mq = messageQueue;
  }

  public String register(Customer customer) {
    return "";
  }
}
