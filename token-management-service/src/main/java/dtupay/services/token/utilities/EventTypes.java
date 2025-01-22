package dtupay.services.token.utilities;

import dtupay.services.token.annotations.ClassAuthor;
import lombok.Getter;

@Getter
@ClassAuthor(author = "Adrian Ursu", stdno = "s240160")
public enum EventTypes {
    TOKENS_REQUESTED("TokensRequested"),
    TOKEN_ACCOUNT_VERIFIED("TokenAccountVerified"),
    TOKEN_ACCOUNT_INVALID("TokenAccountInvalid"),

    CUSTOMER_DEREGISTRATION_REQUESTED("CustomerDeregistrationRequested"),
    CUSTOMER_DEREGISTRATION_FAILURE("CustomerDeregistrationFailure"),
    CUSTOMER_TOKENS_DELETED("CustomerTokensDeleted"),

    TOKENS_GENERATED("TokensGenerated"),
    TOKEN_GENERATION_FAILED("TokenGenerationFailed"),

    PAYMENT_INITIATED("PaymentInitiated"),
    PAYMENT_TOKEN_VERIFIED("PaymentTokenVerified"),
    PAYMENT_TOKEN_INVALID("PaymentTokenInvalid");


    private final String topic;

    EventTypes(String topic) {
        this.topic = topic;
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