package dtupay.model;
public record PaymentRequest(String merchantId, Token token, int amount) {
}
