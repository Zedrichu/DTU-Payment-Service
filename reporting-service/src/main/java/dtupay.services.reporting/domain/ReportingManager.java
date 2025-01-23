package dtupay.services.reporting.domain;

import dtupay.services.reporting.domain.factory.ViewFactory;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.Report;
import dtupay.services.reporting.domain.models.views.CustomerView;
import dtupay.services.reporting.domain.models.views.MerchantView;
import dtupay.services.reporting.domain.repositories.MemoryReportRepository;
import dtupay.services.reporting.domain.repositories.ReportRepository;
import dtupay.services.reporting.utilities.Correlator;
import dtupay.services.reporting.utilities.EventTypes;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportingManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportingManager.class);
    private MessageQueue messageQueue;

    private ReportRepository<CustomerView> customerReportRepository = new MemoryReportRepository<>();
    private ReportRepository<MerchantView> merchantReportRepository = new MemoryReportRepository<>();

    private ViewFactory factory = new ViewFactory();

    public ReportingManager(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;

        this.messageQueue.addHandler(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), this::handleBankTransferConfirmed);
        this.messageQueue.addHandler(EventTypes.CUSTOMER_REPORT_REQUESTED.getTopic(), this::handleCustomerReportRequested);
        this.messageQueue.addHandler(EventTypes.MERCHANT_REPORT_REQUESTED.getTopic(), this::handleMerchantReportRequested);
        this.messageQueue.addHandler(EventTypes.MANAGER_REPORT_REQUESTED.getTopic(), this::handleManagerReportRequested);
    }

    public void handleBankTransferConfirmed(Event event) {
        logger.debug("Received BankTransferConfirmed event: {}", event);
        var paymentRecord = event.getArgument(0, PaymentRecord.class);

        customerReportRepository.addView(paymentRecord.customerId(), factory.createCustomerView(paymentRecord));
        merchantReportRepository.addView(paymentRecord.merchantId(), factory.createMerchantView(paymentRecord));
    }

    public void handleCustomerReportRequested(Event event) {
        logger.debug("Received CustomerRecordRequested event");

        var customerId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, Correlator.class);

        // generate the event with the report back to the facade
        // < id -> ArrayList<Events> (processing)  <Id -> Set<Event>

        var history = customerReportRepository.getReport(customerId);
        Event response = new Event(EventTypes.CUSTOMER_REPORT_GENERATED.getTopic(), new Report<>(history), correlationId);
        this.messageQueue.publish(response);
    }

    public void handleMerchantReportRequested(Event event) {
        logger.debug("Received MerchantRecordRequested event");

        var merchantId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, Correlator.class);

        var history = merchantReportRepository.getReport(merchantId);
        Event response = new Event(EventTypes.CUSTOMER_REPORT_GENERATED.getTopic(), new Report<>(history), correlationId);
        this.messageQueue.publish(response);
    }

    public void handleManagerReportRequested(Event event) {
        logger.debug("Received ManagerReportRequested event");
        var correlationId = event.getArgument(0, Correlator.class);

        var history = customerReportRepository.exportAllManagerViews(this.factory::convertCustomerView);
        Event response = new Event(EventTypes.MANAGER_REPORT_GENERATED.getTopic(), new Report<>(history), correlationId);
        this.messageQueue.publish(response);
    }
}
