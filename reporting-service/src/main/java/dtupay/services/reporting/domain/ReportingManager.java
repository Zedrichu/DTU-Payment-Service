package dtupay.services.reporting.domain;

import dtupay.services.reporting.domain.aggregate.CustomerView;
import dtupay.services.reporting.domain.aggregate.MerchantView;
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
        var paymentRecord = event.getArgument(0, PaymentRecord.class);
        String reportId;
        // First time Customer, Merchant report creation
        var response1 = this.readModelRepository.contains(paymentRecord.merchantId());
        if (!response1.getKey()) {
            reportId = createMerchantReport(paymentRecord);

        }

        var response2 = this.readModelRepository.contains(paymentRecord.customerId());
        if (!response2.getKey()) {
            reportId = createCustomerReport(paymentRecord);
        }
        //updateReport(reportId);


    }


    /* Command Operation */
    /*
    public String createManagerReport(PaymentRecord paymentRecord) {
        // Create a paymentReport
        PaymentReport paymentReport = PaymentReport.createManager(paymentRecord);
        reportRepository.save(paymentReport);
        return paymentReport.getReportId();
    }
    */


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


    /* Query Operation */

    //public Set<CustomerReport> paymentsByCustomerId(String customerId) {
    //    return readModelRepository.getReportsById(customer);
    //}


    // report/{id} ? Or report/{id}/customer

    // aggregate/ User (aggregate) -> UserId (aggregate root)
    //                             -> Address
    //                             -> Contact

    // aggregate/ Report (aggregated) -> TransactionId (aggregate root)





    //public Set<CustomerReport> paymentByCustomerId(String cId) {

}
