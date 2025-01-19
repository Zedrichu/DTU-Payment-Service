package dtupay.services.token;

import messaging.MessageQueue;

public class TokenManager {
    private MessageQueue mque;

    public TokenManager(MessageQueue messageQueue) {
        this.mque = messageQueue;
    }
}
