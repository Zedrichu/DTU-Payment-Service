package dtupay.services.payment.domain.models;

public record PaymentRequest(String merchantId, String token, int amount) {
}
