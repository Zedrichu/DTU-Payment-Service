package dtupay.services.account.utilities;

import dtupay.services.account.annotations.ClassAuthor;

@ClassAuthor(author = "Adrian Ursu", stdno = "s240160")
public enum EventTypes {
    CUSTOMER_REGISTRATION_REQUESTED("CustomerRegistrationRequested"),
    CUSTOMER_ACCOUNT_CREATED("CustomerAccountCreated"),
    CUSTOMER_ACCOUNT_VERIFIED("CustomerAccountVerified"),
    CUSTOMER_ACCOUNT_INVALID("CustomerAccountInvalid"),
    CUSTOMER_ACCOUNT_CREATION_FAILED("CustomerAccountCreationFailed"),
    CUSTOMER_DEREGISTRATION_REQUESTED("CustomerDeregistrationRequested"),
    CUSTOMER_DELETED("CustomerDeleted"),
    CUSTOMER_DELETED_FAILED("CustomerDeleteFailed"),
    
    MERCHANT_ACCOUNT_VERIFIED("MerchantAccountVerified"),
    MERCHANT_ACCOUNT_INVALID("MerchantAccountInvalid"),
    MERCHANT_REGISTRATION_REQUESTED("MerchantRegistrationRequested"),
    MERCHANT_ACCOUNT_CREATED("MerchantAccountCreated"),
    MERCHANT_ACCOUNT_CREATION_FAILED("MerchantAccountCreationFailed"),
    MERCHANT_DEREGISTRATION_REQUESTED("MerchantDeregistrationRequested"),
    MERCHANT_DELETED("MerchantDeleted"),
    MERCHANT_DELETED_FAILED("MerchantDeleteFailed"),

    PAYMENT_INITIATED("PaymentInitiated"),
    PAYMENT_TOKEN_VERIFIED("PaymentTokenVerified"),

    TOKEN_ACCOUNT_INVALID("TokenAccountInvalid"),

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
        throw new IllegalArgumentException("Invalid event type: " + topic);
    }
}
