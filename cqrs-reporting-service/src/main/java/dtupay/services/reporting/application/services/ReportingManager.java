package dtupay.services.reporting.application.services;

import dtupay.services.reporting.adapters.persistence.LedgerWriteRepository;
import dtupay.services.reporting.annotations.MethodAuthor;
import dtupay.services.reporting.domain.entities.LedgerAggregate;
import dtupay.services.reporting.domain.entities.ReportingRole;
import dtupay.services.reporting.models.PaymentRecord;
import dtupay.services.reporting.query.projection.ReportProjection;
import dtupay.services.reporting.query.repositories.LedgerReadRepository;
import dtupay.services.reporting.query.views.CustomerView;
import dtupay.services.reporting.query.views.ManagerView;
import dtupay.services.reporting.query.views.MerchantView;
import dtupay.services.reporting.utilities.Correlator;
import dtupay.services.reporting.utilities.EventTypes;
import lombok.Getter;
import messaging.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;


/* See CQRS scheme logic: resources/ledger-cqrs.txt */
/* See <<CQRS+EventSourcing>> architecture diagram: resources/.png */
public class ReportingManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportingManager.class);
    @Getter
    private final LedgerAggregate aggregate;
    @Getter
    private final ReportProjection projection;
    private final messaging.MessageQueue dtupayMQ;

    public ReportingManager(messaging.MessageQueue messageQueue, LedgerReadRepository readRepository, LedgerWriteRepository writeRepository) {
        this.dtupayMQ = messageQueue;

        this.aggregate = new LedgerAggregate(writeRepository);
        this.projection = new ReportProjection(readRepository);

        this.dtupayMQ.addHandler(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), this::handleBankTransferConfirmed);
        this.dtupayMQ.addHandler(EventTypes.CUSTOMER_REPORT_REQUESTED.getTopic(), this::handleCustomerReportRequested);
        this.dtupayMQ.addHandler(EventTypes.MERCHANT_REPORT_REQUESTED.getTopic(), this::handleMerchantReportRequested);
        this.dtupayMQ.addHandler(EventTypes.MANAGER_REPORT_REQUESTED.getTopic(), this::handleManagerReportRequested);

        aggregate.createLedger("ADMIN", ReportingRole.MANAGER);
    }

    public void handleCustomerReportRequested(Event event) {
        logger.debug("Received CustomerReportRequested event: {}", event);
        var customerId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, Correlator.class);

        // generate the event with the report back to the facade
        ArrayList<CustomerView> views = new ArrayList<>(projection.getCustomerViews(customerId));
        Event response = new Event(EventTypes.CUSTOMER_REPORT_GENERATED.getTopic(), views, correlationId);
        this.dtupayMQ.publish(response);
    }

    public void handleMerchantReportRequested(Event event) {
        logger.debug("Received MerchantReportRequested event: {}", event);
        var merchantId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, Correlator.class);

        // generate the event with the report back to the facade
        ArrayList<MerchantView> views = new ArrayList<>(projection.getMerchantViews(merchantId));
        Event response = new Event(EventTypes.MERCHANT_REPORT_GENERATED.getTopic(), views, correlationId);
        this.dtupayMQ.publish(response);
    }

    public void handleManagerReportRequested(Event event) {
        logger.debug("Received ManagerReportRequested event: {}", event);
        var correlationId = event.getArgument(0, Correlator.class);

        // generate the event with the report back to the facade
        ArrayList<ManagerView> views = new ArrayList<>(projection.getManagerViews());

        Event response = new Event(EventTypes.MANAGER_REPORT_GENERATED.getTopic(), views, correlationId);
        this.dtupayMQ.publish(response);
    }

    @MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
    public void handleBankTransferConfirmed(Event event) {
        logger.debug("Received BankTransferConfirmed event: {}", event);
        var payLog = event.getArgument(0, PaymentRecord.class);

        // First time Customer, Merchant ledger creation
        var response1 = projection.contains(payLog.merchantId());
        if (!response1) {
            aggregate.createLedger(payLog.merchantId(), ReportingRole.MERCHANT);
        }

        var response2 = projection.contains(payLog.customerId());
        if (!response2) {
            aggregate.createLedger(payLog.customerId(), ReportingRole.CUSTOMER);
        }

        logPayment(payLog);
    }

    /* Helper methods */
    private void logPayment(PaymentRecord transaction) {
        aggregate.updateLedger(transaction.merchantId(), Set.of(transaction));
        aggregate.updateLedger(transaction.customerId(), Set.of(transaction));
        aggregate.updateLedger("ADMIN", Set.of(transaction));
    }

    /*____________________________________________________________________________________*/
    // Commands: CreateLedgerCommand<id, role>, UpdateLedgerCommand<id, transactions>
    /* Command Operations */
    // ledgerAggregate.createLedger(id, role)
    // ledgerAggregate.updateLedger(id, transactions)

    /*_________________________________________________________________________*/
    // Queries: CustomerViewsById<id>, MerchantViewsById<id>, ManagerViews
    /* Query Operations */
    // reportProjection.getCustomerViews(id)
    // reportProjection.getMerchantViews(id)
    // reportProjection.getManagerViews()
}