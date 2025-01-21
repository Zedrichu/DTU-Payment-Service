package dtupay.services.token.utilities;

import dtupay.services.token.annotations.ClassAuthor;
import lombok.Getter;

@Getter
@ClassAuthor(author = "Adrian Ursu", stdno = "s240160")
public enum EventTypes {
    TOKENS_REQUESTED("TokensRequested"),
    TOKEN_ACCOUNT_VERIFIED("TokenAccountVerified"),
    TOKEN_ACCOUNT_INVALID("TokenAccountInvalid"),

    TOKENS_GENERATED("TokensGenerated"),
    TOKEN_GENERATION_FAILURE("TokenGenerationFailure"),

    PAYMENT_INITIATED("PaymentInitiated"),
    PAYMENT_TOKEN_VERIFIED("PaymentTokenVerified");


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