package dtupay.services.payment.domain.models;

import dtupay.services.payment.utilities.Correlator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode
public class BankTransferAggregate {

    // Aggregate root - identifies the whole structure
    private Correlator correlator;
    @Setter
    private Merchant merchant;
    @Setter
    private Customer customer;
    @Setter
    private PaymentRequest paymentRequest;

    BankTransferAggregate() {
        super();
    }

    public BankTransferAggregate(Correlator id) {
        this.correlator = id;
    }

    public boolean isComplete(){
        return customer != null & merchant != null & paymentRequest != null;
    }
}
