package dtupay.services.reporting.domain;

import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.repositories.ReadModelRepository;
import dtupay.services.reporting.domain.repositories.PaymentLogRepository;
import dtupay.services.reporting.utilities.EventTypes;
import messaging.Event;
import messaging.MessageQueue;
import dtupay.services.reporting.domain.aggregate.CustomerReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ReportingManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportingManager.class);
    private ReadModelRepository readModelRepository;
    private PaymentLogRepository paymentLogRepository;
    private MessageQueue dtupayMQ;

    public ReportingManager(MessageQueue messageQueue, ReadModelRepository readRepository, PaymentLogRepository writeRepository) {
        this.readModelRepository = readRepository;
        this.paymentLogRepository = writeRepository;
        this.dtupayMQ = messageQueue;


        this.dtupayMQ.addHandler(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), this::handleBankTransferConfirmed);
        this.dtupayMQ.addHandler(EventTypes.CUSTOMER_REPORT_REQUESTED.getTopic(), this::handleCustomerRecordRequested);
    }

    public void handleCustomerRecordRequested(Event event) {
        logger.debug("Received CustomerReportRequested event: {}", event);
        var customerId = event.getArgument(0, String.class);

        // generate the event with the report back to the facade
    }

    public void handleBankTransferConfirmed(Event event) {
        logger.debug("Received BankTransferConfirmed event: {}", event);
        var paymentRecord = event.getArgument(0, PaymentRecord.class);

        //TODO: Implement the logic to record the payment of Merchant and Manager
        this.recordCustomerPayment(paymentRecord);
    }


    /* Command Operation */
    public void recordCustomerPayment(PaymentRecord paymentRecord) {
        CustomerReport customerReport = CustomerReport.create(paymentRecord);
        paymentLogRepository.save(customerReport);
    }

    /* Query Operation */

    public Set<CustomerReport> paymentsByCustomerId(String customerId) {
        return readModelRepository.getReportsById(customer);
    }



    //public Set<CustomerReport> paymentByCustomerId(String cId) {

}
