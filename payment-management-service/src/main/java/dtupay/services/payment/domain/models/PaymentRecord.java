package dtupay.services.payment.domain.models;

import dtupay.services.payment.annotations.ClassAuthor;

@ClassAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
public record PaymentRecord(String customerBankAccount, String merchantBankAccount, int amount, String description,
                            Token token) {
}
