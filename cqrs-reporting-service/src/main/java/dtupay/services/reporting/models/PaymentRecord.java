package dtupay.services.reporting.models;

import java.io.Serializable;

// <<Value>> Immutable object, identified by attributes
public record PaymentRecord(
    String customerBankAccount,
    String merchantBankAccount,
    int amount,
    String description,
    Token token,
    String customerId,
    String merchantId) implements Serializable {}