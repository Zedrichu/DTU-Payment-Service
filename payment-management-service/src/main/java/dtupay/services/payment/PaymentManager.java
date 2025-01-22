package dtupay.services.payment;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import dtupay.services.payment.domain.models.*;
import dtupay.services.payment.utilities.Correlator;
import dtupay.services.payment.utilities.EventTypes;

import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentManager {
    private static final Logger logger = LoggerFactory.getLogger(PaymentManager.class);

    private MessageQueue mque;
    private Map<Correlator, BankTransferAggregate> aggregators = new ConcurrentHashMap<>();
    private BankService bankService = new BankServiceService().getBankServicePort();


    public PaymentManager(MessageQueue messageQueue) {
        logger.debug("Initializing Payment Manager");

        this.mque = messageQueue;

        // Add handlers
        this.mque.addHandler(EventTypes.PAYMENT_INITIATED.getTopic(), this::handlePaymentInitiated);
        this.mque.addHandler(EventTypes.CUSTOMER_ACCOUNT_VERIFIED.getTopic(), this::handleCustomerAccountVerified);
        this.mque.addHandler(EventTypes.MERCHANT_ACCOUNT_VERIFIED.getTopic(), this::handleMerchantAccountVerified);
    }

    public synchronized BankTransferAggregate getOrCreateAggregate(Correlator correlator) {
        if (!aggregators.containsKey(correlator)) {
            aggregators.put(correlator,new BankTransferAggregate(correlator));
        }
        return aggregators.get(correlator);
    }

    public void completePayment(BankTransferAggregate aggregate){
        logger.debug("Completing payment for aggregate: {}",aggregate);
        if (aggregate.isComplete()) {
            logger.debug("Payment completed successfully");
            String merchantBankAccount = aggregate.getMerchant().bankAccountNo();
            String customerBankAccount = aggregate.getCustomer().bankAccountNo();
            String description = "DTUPay\n Used token |> " + aggregate.getPaymentRequest().token();
            BigDecimal amount = BigDecimal.valueOf(aggregate.getPaymentRequest().amount());

            Event responseEvent;
            // SOAP Bank call
            try {
                bankService.transferMoneyFromTo(customerBankAccount, merchantBankAccount, amount, description);
                logger.debug("Bank Transfer completed successfully");
                PaymentRecord paymentRecord = new PaymentRecord(customerBankAccount,
                        merchantBankAccount,aggregate.
                        getPaymentRequest().amount(),
                        description,
                        aggregate.getPaymentRequest().token());
                responseEvent = new Event(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(),new Object[]{ paymentRecord, aggregate.getCorrelator() });
            } catch (BankServiceException_Exception e) {
                responseEvent = new Event(EventTypes.BANK_TRANSFER_FAILED.getTopic(), new Object[] { e.getMessage(), aggregate.getCorrelator() });
            }

            mque.publish(responseEvent);
        }

    }

    public void handlePaymentInitiated(Event event) {
        logger.debug("Received PaymentInitiated event: {}", event);
        PaymentRequest paymentRequest = event.getArgument(0, PaymentRequest.class);
        Correlator correlator = event.getArgument(1,Correlator.class);
        BankTransferAggregate aggregate = getOrCreateAggregate(correlator);
        aggregate.setPaymentRequest(paymentRequest);
        completePayment(aggregate);
    }

    public void handleCustomerAccountVerified(Event event) {
        logger.debug("Received CustomerAccountVerified event: {}", event);
        Customer customer = event.getArgument(0, Customer.class);
        Correlator correlator = event.getArgument(1,Correlator.class);
        BankTransferAggregate aggregate = getOrCreateAggregate(correlator);
        aggregate.setCustomer(customer);
        completePayment(aggregate);
    }

    public void handleMerchantAccountVerified(Event event) {
        logger.debug("Received MerchantAccountVerified event: {}", event);
        Merchant merchant = event.getArgument(0, Merchant.class);
        Correlator correlator = event.getArgument(1,Correlator.class);
        BankTransferAggregate aggregate = getOrCreateAggregate(correlator);
        aggregate.setMerchant(merchant);
        completePayment(aggregate);
    }
}
