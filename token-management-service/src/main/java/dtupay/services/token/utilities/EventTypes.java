package dtupay.services.token.utilities;

public enum EventTypes {
    TOKENS_REQUESTED("TokensRequested"),
    TOKEN_ACCOUNT_VERIFIED("TokenAccountVerified"),
    TOKENS_GENERATED("TokensGenerated"),

    PAYMENT_INITIATED("PaymentInitiated"),
    PAYMENT_TOKEN_VERIFIED("PaymentTokenVerified");

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