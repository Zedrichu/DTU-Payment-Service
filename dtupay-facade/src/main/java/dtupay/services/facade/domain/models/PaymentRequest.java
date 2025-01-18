package dtupay.services.facade.domain.models;

public record PaymentRequest(String merchantId, String token, int amount) {
}
