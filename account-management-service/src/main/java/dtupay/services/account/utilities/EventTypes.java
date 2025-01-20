package dtupay.services.account.utilities;

public enum EventTypes {
    CUSTOMER_REGISTRATION_REQUESTED("CustomerRegistrationRequested"),
    CUSTOMER_ACCOUNT_CREATED("CustomerAccountCreated"),
    CUSTOMER_ACCOUNT_VERIFIED("CustomerAccountVerified"),
    CUSTOMER_ACCOUNT_CREATION_FAILED("CustomerAccountCreationFailed"),
    
    MERCHANT_ACCOUNT_VERIFIED("MerchantAccountVerified"),
    MERCHANT_REGISTRATION_REQUESTED("MerchantRegistrationRequested"),
    MERCHANT_ACCOUNT_CREATED("MerchantAccountCreated"),

    PAYMENT_INITIATED("PaymentInitiated"),
    PAYMENT_TOKEN_VERIFIED("PaymentTokenVerified"),

    TOKENS_REQUESTED("TokensRequested"),
    TOKEN_ACCOUNT_VERIFIED("TokenAccountVerified");

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
