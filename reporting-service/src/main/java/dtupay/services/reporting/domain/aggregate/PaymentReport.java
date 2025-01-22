package dtupay.services.reporting.domain.aggregate;

import dtupay.services.reporting.domain.events.PaymentCustomerAdded;
import dtupay.services.reporting.domain.models.PaymentRecord;
import lombok.Getter;
import messaging.Event;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PaymentReport {
    private String rId;
    private List<Event> appliedEvents = new ArrayList<Event>();

    public static PaymentReport create(PaymentRecord paymentRecord) {
        PaymentCustomerAdded event = new PaymentCustomerAdded(
                paymentRecord.amount(),
                paymentRecord.token(),
                paymentRecord.merchantId()
        );
        var customerReport = new PaymentReport();
        customerReport.rId = paymentRecord.customerId();
        customerReport.appliedEvents.add(event);
        return customerReport;
    }


    public void clearAppliedEvents() { appliedEvents.clear();
    }
}
