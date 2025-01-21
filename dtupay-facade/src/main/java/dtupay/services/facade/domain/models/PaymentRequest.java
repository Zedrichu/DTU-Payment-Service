package dtupay.services.facade.domain.models;


public record PaymentRequest(String merchantId, Token token, int amount) {
}
