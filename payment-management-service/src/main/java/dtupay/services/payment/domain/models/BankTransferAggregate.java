package dtupay.services.payment.domain.models;

import dtupay.services.payment.utilities.Correlator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

@Getter(onMethod_={@Synchronized})
@Setter(onMethod_={@Synchronized})
@EqualsAndHashCode
public class BankTransferAggregate {

    // Aggregate root - identifies the whole structure
    private Correlator correlator;


    private Merchant merchant;
    private Customer customer;
    private PaymentRequest paymentRequest;

    BankTransferAggregate() {
        super();
    }

    public BankTransferAggregate(Correlator id) {
        this.correlator = id;
    }

    @Synchronized
    public synchronized boolean isComplete(){
        return customer != null & merchant != null & paymentRequest != null;
    }
}
