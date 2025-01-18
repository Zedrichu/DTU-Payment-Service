package dtupay.services.account.domain.models;

public record PaymentRequest(String merchantId, String token, int amount) {
}
