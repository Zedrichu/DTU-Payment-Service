package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.aggregate.PaymentReport;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;

public class ReportRepository {

    private EventStore eventStore;

    public ReportRepository(MessageQueue bus) {
        eventStore = new EventStore(bus);
    }


    public void save(PaymentReport paymentReport) {
        eventStore.addEvents(paymentReport.getReportId(), paymentReport.getAppliedEvents());
        paymentReport.clearAppliedEvents();
    }


    public PaymentReport getById(String reportId) {
        return PaymentReport.createFromEvents(eventStore.getEventsFor(reportId));
    }
}
