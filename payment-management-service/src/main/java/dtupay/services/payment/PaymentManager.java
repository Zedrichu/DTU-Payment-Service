package dtupay.services.payment;

import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentManager {
    private static final Logger logger = LoggerFactory.getLogger(PaymentManager.class);

    private MessageQueue mque;

    public PaymentManager(MessageQueue messageQueue) {
        logger.debug("InitializinPaymentnt Manager");

        this.mque = messageQueue;

        // Add handlers
        this.mque.addHandler("PaymentInitiated", this::handlePaymentInitiated);
        this.mque.addHandler("CustomerAccountVerified", this::handleCustomerAccountVerified);
        this.mque.addHandler("MerchantAccountVerified", this::handleMerchantAccountVerified);
    }

    public void handlePaymentInitiated(Event event) {}

    public void handleCustomerAccountVerified(Event event) {}

    public void handleMerchantAccountVerified(Event event) {}
}


//    bankService.transferMoneyFromTo(customer.bankAccountNo(), merchant.bankAccountNo(), BigDecimal.valueOf(paymentRequest.amount()));
