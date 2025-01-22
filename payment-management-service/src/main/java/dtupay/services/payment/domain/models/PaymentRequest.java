package dtupay.services.payment.domain.models;

public record PaymentRequest(String merchantId, Token token, int amount) {
}
