package dtupay.services.token.models;

public record PaymentRequest(String merchantId, Token token, int amount) {
}
