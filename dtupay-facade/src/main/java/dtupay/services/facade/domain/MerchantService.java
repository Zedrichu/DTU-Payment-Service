package dtupay.services.facade.domain;

import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.utilities.Correlator;
import messaging.MessageQueue;
import messaging.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MerchantService {

  private Logger logger = LoggerFactory.getLogger(MerchantService.class);
  private MessageQueue mque;
  private Map<Correlator, CompletableFuture<Merchant>> correlations = new ConcurrentHashMap<>();

  public MerchantService(MessageQueue messageQueue) {
    logger.info("facade.MerchantService instantiated");
    this.mque = messageQueue;

    this.mque.addHandler("MerchantAccountCreated", this::handleMerchantAccountCreated);
  }

  public Merchant register(Merchant merchant) {
    logger.debug("Registration request for: {}", merchant);
    var correlationId = Correlator.random();
    correlations.put(correlationId, new CompletableFuture<>());
    Event event = new Event("MerchantRegistrationRequested", new Object[] { merchant, correlationId });
    mque.publish(event);
    return correlations.get(correlationId).join();
  }

  public void handleMerchantAccountCreated(Event event) {
    logger.debug("Received MerchantAccountCreated event: {}", event);
    var reqMerchant = event.getArgument(0, Merchant.class);
    var correlationId = event.getArgument(1, Correlator.class);
    correlations.get(correlationId).complete(reqMerchant);
  }
}
