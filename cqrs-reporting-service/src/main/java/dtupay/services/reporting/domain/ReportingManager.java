package dtupay.services.reporting.domain;

import dtupay.services.reporting.domain.aggregate.views.CustomerView;
import dtupay.services.reporting.domain.aggregate.views.ManagerView;
import dtupay.services.reporting.domain.aggregate.views.MerchantView;
import dtupay.services.reporting.domain.aggregate.PaymentReport;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.repositories.ReadModelRepository;
import dtupay.services.reporting.domain.repositories.ReportRepository;
import dtupay.services.reporting.utilities.EventTypes;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ReportingManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportingManager.class);
    private ReadModelRepository readModelRepository;
    private ReportRepository reportRepository;
    private MessageQueue dtupayMQ;

    public ReportingManager(MessageQueue messageQueue, ReadModelRepository readRepository, ReportRepository writeRepository) {
        this.readModelRepository = readRepository;
        this.reportRepository = writeRepository;
        this.dtupayMQ = messageQueue;


        this.dtupayMQ.addHandler(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), this::handleBankTransferConfirmed);
        this.dtupayMQ.addHandler(EventTypes.CUSTOMER_REPORT_REQUESTED.getTopic(), this::handleCustomerRecordRequested);

        PaymentReport initManager = PaymentReport.createManager();
        reportRepository.save(initManager);
    }

    public void handleCustomerRecordRequested(Event event) {
        logger.debug("Received CustomerReportRequested event: {}", event);
        var customerId = event.getArgument(0, String.class);

        // generate the event with the report back to the facade

        // < id -> ArrayList<Events> (processing)  <Id -> Set<Event>
    }

    public void handleBankTransferConfirmed(Event event) {
        logger.debug("Received BankTransferConfirmed event: {}", event);
        var pay = event.getArgument(0, PaymentRecord.class);

        CustomerView cView = new CustomerView(pay.amount(), pay.merchantId(), pay.token());
        MerchantView mView = new MerchantView(pay.amount(), pay.token());
        String reportId;

        // First time Customer, Merchant report creation
        var response1 = this.readModelRepository.contains(pay.merchantId());
        if (!response1.getKey()) {
            reportId = createMerchantReport(pay);
        }
        logPayment(pay.merchantId(), mView);

        var response2 = this.readModelRepository.contains(pay.customerId());
        if (!response2.getKey()) {
            reportId = createCustomerReport(pay);
        }
        logPayment(pay.customerId(), cView);

        logPayment("admin", mView);
        logPayment("admin", cView);
    }

    /* Helper methods */
    public void logPayment(String customerId, CustomerView customerView) {
        updateReport(customerId, Set.of(customerView), Set.of());
    }

    public void logPayment(String customerId, MerchantView merchantView) {
        updateReport(customerId, Set.of(), Set.of(merchantView));
    }

    /*____________________________________________________________________________________*/
    /* Command Operations */
    public String createCustomerReport(PaymentRecord paymentRecord) {
        // Create a paymentReport
        PaymentReport paymentReport = PaymentReport.createCustomer(paymentRecord);
        reportRepository.save(paymentReport);
        return paymentReport.getReportId();
    }

    public String createMerchantReport(PaymentRecord paymentRecord) {
        // Create a paymentReport
        PaymentReport paymentReport = PaymentReport.createMerchant(paymentRecord);
        reportRepository.save(paymentReport);
        return paymentReport.getReportId();
    }

    // < id --> Set<event>
    public void updateReport(String reportId, Set<CustomerView> customerViews, Set<MerchantView> merchantViews) {
        PaymentReport paymentReport = reportRepository.getById(reportId);
        paymentReport.update(customerViews, merchantViews);
        reportRepository.save(paymentReport);
    }

    /*_________________________________________________________________________*/
    /* Query Operations */
    public Set<CustomerView> customerViews(String customerId) {
        return readModelRepository.getCustomerViews(customerId);
    }

    public Set<MerchantView> merchantViews(String customerId) {
        return readModelRepository.getMerchantViews(customerId);
    }

    public Set<ManagerView> managerViews() {
        return readModelRepository.getAllManagerViews();
    }

    // aggregate/ User (aggregate) -> UserId (aggregate root)
    //                             -> Address
    //                             -> Contact

    // aggregate/ Report (aggregated) -> TransactionId (aggregate root)

    // Report - customerId, token, amount, merchantId, customerBank, merchantBank, description
    // -> Token (aggregate root)
    // repo.save(report)
    // return report.getId() -> Token

    // manager: all fields -> Set<Report>
    // customer: <amount, merchantId, token> with filter <customerId> on report
    // merchant: <amount, token> with filter <merchantId> on report
    // others: <customerId, customerBank, merchantBank, description>

    // Report (aggregate) -> ReportId(token), CustomerView(...), MerchantView(...), HiddenView(...)
    // Report repository:
    // 		Set <CustomerView> customerReportsByCustomerId(ReportId, String customerId)
    // 		Set <MerchantView> merchantReportsByMerchantId(ReportId, String merchantId)
}
