package dtupay.services.facade.domain;

import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.domain.models.PaymentRequest;
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
  private Map<Correlator, CompletableFuture<Merchant>> registerCorrelations = new ConcurrentHashMap<>();
  private Map<Correlator, CompletableFuture<Boolean>> payCorrelations = new ConcurrentHashMap<>();

  public MerchantService(MessageQueue messageQueue) {
    logger.info("facade.MerchantService instantiated");
    this.mque = messageQueue;

    this.mque.addHandler("MerchantAccountCreated", this::handleMerchantAccountCreated);
  }

  public Merchant register(Merchant merchant) {
    logger.debug("Registration request for: {}", merchant);
    var correlationId = Correlator.random();
    registerCorrelations.put(correlationId, new CompletableFuture<>());
    Event event = new Event("MerchantRegistrationRequested", new Object[] { merchant, correlationId });
    mque.publish(event);
    return registerCorrelations.get(correlationId).join();
  }

  public boolean pay(PaymentRequest paymentRequest) {
    logger.debug("Pay request received: {}", paymentRequest);
    var correlationId = Correlator.random();
    payCorrelations.put(correlationId, new CompletableFuture<>());

    Event event = new Event("PaymentInitiated", new Object[] { paymentRequest, correlationId });
    mque.publish(event);

    return payCorrelations.get(correlationId).join();
  }

  public void handleMerchantAccountCreated(Event event) {
    logger.debug("Received MerchantAccountCreated event: {}", event);
    var reqMerchant = event.getArgument(0, Merchant.class);
    var correlationId = event.getArgument(1, Correlator.class);
    registerCorrelations.get(correlationId).complete(reqMerchant);
  }

  public void handleBankTransferConfirmed(Event event) {
    logger.debug("Received BankTransferConfirmed event: {}", event);
    var core = event.getArgument(1, Correlator.class);

    payCorrelations.get(core).complete(true);
  }
}
