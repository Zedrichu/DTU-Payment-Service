package dtupay.services.payment.utilities;

import dtupay.services.payment.annotations.ClassAuthor;

@ClassAuthor(author = "Adrian Ursu", stdno = "s240160")
public enum EventTypes {
    PAYMENT_INITIATED("PaymentInitiated"),

    CUSTOMER_ACCOUNT_VERIFIED("CustomerAccountVerified"),

    MERCHANT_ACCOUNT_VERIFIED("MerchantAccountVerified"),

    BANK_TRANSFER_CONFIRMED("BankTransferConfirmed"),
    BANK_TRANSFER_FAILED("BankTransferFailed");

    private final String topic;

    EventTypes(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public static EventTypes fromTopic(String topic) {
        for (EventTypes eventType : values()) {
            if (eventType.topic.equals(topic)) {
              return eventType;
            }
        }
        throw new IllegalArgumentException("No matching EventTypes for topic: " + topic);
    }

    @Override
    public String toString() {
        return topic;
    }
}