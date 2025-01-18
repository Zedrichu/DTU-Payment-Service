package dtupay.model;

public record PaymentRequest(String merchantId, String token, int amount) {
}
