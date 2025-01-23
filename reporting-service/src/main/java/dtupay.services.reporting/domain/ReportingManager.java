package dtupay.services.reporting.domain;

import dtupay.services.reporting.domain.factory.ViewFactory;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.views.CustomerView;
import dtupay.services.reporting.domain.models.views.MerchantView;
import dtupay.services.reporting.domain.repositories.LinearReportRepository;
import dtupay.services.reporting.domain.repositories.MemoryReportRepository;
import dtupay.services.reporting.domain.repositories.ReportRepository;
import dtupay.services.reporting.utilities.EventTypes;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportingManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportingManager.class);
    private MessageQueue messageQueue;

    private LinearReportRepository managerRepository = new LinearReportRepository();
    private ReportRepository<CustomerView> customerReportRepository = new MemoryReportRepository<>();
    private ReportRepository<MerchantView> merchantReportRepository = new MemoryReportRepository<>();

    private ViewFactory factory = new ViewFactory();

    public ReportingManager(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;

        this.messageQueue.addHandler(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), this::handleBankTransferConfirmed);
        this.messageQueue.addHandler(EventTypes.CUSTOMER_REPORT_REQUESTED.getTopic(), this::handleCustomerRecordRequested);
    }

    public void handleCustomerRecordRequested(Event event) {
        logger.debug("Received CustomerReportRequested event: {}", event);
        var customerId = event.getArgument(0, String.class);

        // generate the event with the report back to the facade

        // < id -> ArrayList<Events> (processing)  <Id -> Set<Event>
    }

    public void handleBankTransferConfirmed(Event event) {
        logger.debug("Received BankTransferConfirmed event: {}", event);
        var paymentRecord = event.getArgument(0, PaymentRecord.class);

        managerRepository.addView(factory.createManagerView(paymentRecord));
        customerReportRepository.addView(paymentRecord.customerId(), factory.createCustomerView(paymentRecord));
        merchantReportRepository.addView(paymentRecord.customerId(), factory.createMerchantView(paymentRecord));
    }
}
