package dtupay.services.payment.domain.models;

import dtupay.services.payment.utilities.Correlator;

public record BankTransferAggregator(Correlator correlationId,
                                     PaymentRequest paymentRequest,
                                     Merchant merchant, Customer customer) {}
