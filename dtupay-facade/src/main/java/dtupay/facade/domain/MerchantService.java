package dtupay.facade.domain;

import dtupay.facade.domain.models.Merchant;
import messaging.MessageQueue;
import messaging.Event;

import java.util.concurrent.CompletableFuture;

public class MerchantService {

  private MessageQueue mque;
  private CompletableFuture<Merchant> registeredMerchant;

  public MerchantService(MessageQueue messageQueue) {
    this.mque = messageQueue;
  }

  public Merchant register(Merchant merchant) {
    registeredMerchant = new CompletableFuture<>();
    Event event = new Event("", new Object[] { merchant });
    mque.publish(event);
    return registeredMerchant.join();
  }

}
