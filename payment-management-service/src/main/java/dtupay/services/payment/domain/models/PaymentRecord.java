package dtupay.services.payment.domain.models;

public record PaymentRecord(String customerBankAccount, String merchantBankAccount, int amount, String description,
                            Token token) {
}
