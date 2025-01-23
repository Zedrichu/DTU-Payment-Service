package dtupay.services.facade.domain;

import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.domain.models.PaymentRequest;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.exception.AccountDeletionException;
import dtupay.services.facade.exception.BankFailureException;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import messaging.MessageQueue;
import messaging.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class MerchantService {

  private Logger logger = LoggerFactory.getLogger(MerchantService.class);
  private MessageQueue mque;
  private Map<Correlator, CompletableFuture<Merchant>> registerCorrelations = new ConcurrentHashMap<>();
  private Map<Correlator, CompletableFuture<Boolean>> payCorrelations = new ConcurrentHashMap<>();
  private Map<Correlator, CompletableFuture<Boolean>> deregistrationCorrelations = new ConcurrentHashMap<>();

  public MerchantService(MessageQueue messageQueue) {
    logger.info("facade.MerchantService instantiated");
    this.mque = messageQueue;

    this.mque.addHandler(EventTypes.MERCHANT_ACCOUNT_CREATED.getTopic(), this::handleMerchantAccountCreated);
    this.mque.addHandler(EventTypes.MERCHANT_ACCOUNT_CREATION_FAILED.getTopic(), this::handleMerchantAccountCreationFailed);
    this.mque.addHandler(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), this::handleBankTransferConfirmed);
    this.mque.addHandler(EventTypes.BANK_TRANSFER_FAILED.getTopic(), this::handleBankTransferFailed);
    this.mque.addHandler(EventTypes.MERCHANT_DELETED.getTopic(), this::handleMerchantDeregistered);
    this.mque.addHandler(EventTypes.MERCHANT_DELETED_FAILED.getTopic(), this::handleMerchantDeregistered);
  }

  public Merchant register(Merchant merchant) throws CompletionException {
    logger.debug("Registration request for: {}", merchant);
    var correlationId = Correlator.random();
    registerCorrelations.put(correlationId, new CompletableFuture<>());
    Event event = new Event(EventTypes.MERCHANT_REGISTRATION_REQUESTED.getTopic(), new Object[] { merchant, correlationId });
    mque.publish(event);
    return registerCorrelations.get(correlationId).join();
  }

  public boolean pay(PaymentRequest paymentRequest) throws CompletionException {
    logger.debug("Pay request received: {}", paymentRequest);
    var correlationId = Correlator.random();
    payCorrelations.put(correlationId, new CompletableFuture<>());

    Event event = new Event(EventTypes.PAYMENT_INITIATED.getTopic(), paymentRequest, correlationId);
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

  public void handleMerchantAccountCreationFailed(Event event) {
    logger.debug("Received MerchantAccountCreationFailed event: {}", event);
    var errorMessage = event.getArgument(0, String.class);
    var core = event.getArgument(1, Correlator.class);
    registerCorrelations.get(core).completeExceptionally(new CompletionException(new AccountCreationException(errorMessage)));
  }

  public void handleBankTransferFailed(Event event) {
    logger.debug("Received BankTransferFailed event: {}", event);
    var errorMessage = event.getArgument(0, String.class);
    var correlationId = event.getArgument(1, Correlator.class);
    payCorrelations.get(correlationId).completeExceptionally(new BankFailureException(errorMessage));
  }

  public boolean deregister(String merchantId)  throws AccountDeletionException {
    logger.debug("Deregister request for merchant: {}", merchantId);
    var correlationId = Correlator.random();
    deregistrationCorrelations.put(correlationId, new CompletableFuture<>());
    Event event = new Event(EventTypes.MERCHANT_DEREGISTRATION_REQUESTED.getTopic(), new Object[]{ merchantId, correlationId });
    mque.publish(event);
    return deregistrationCorrelations.get(correlationId).join();
  }

  public void handleMerchantDeregistered(Event event) {
    logger.debug("Received Merchant Deregistered event: {}", event);
    var correlationId = event.getArgument(0, Correlator.class);
    if (event.getTopic().equals(EventTypes.MERCHANT_DELETED.getTopic())) {
      deregistrationCorrelations.get(correlationId).complete(true);
    } else {
      deregistrationCorrelations.get(correlationId).completeExceptionally(new AccountCreationException("Merchant Deregistration Failed"));
    }
  }
}
