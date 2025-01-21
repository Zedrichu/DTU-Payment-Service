package dtupay.services.token.domain.models;

public record PaymentRequest(String merchantId, Token token, int amount) {}
