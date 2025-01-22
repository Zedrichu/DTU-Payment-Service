package dtupay.services.account.domain.models;

public record PaymentRequest(String merchantId, Token token, int amount) {
}
