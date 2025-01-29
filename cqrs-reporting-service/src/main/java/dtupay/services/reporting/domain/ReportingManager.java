package dtupay.services.reporting.domain;

import dtupay.services.reporting.domain.aggregate.Ledger;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.aggregate.ReportingRole;
import dtupay.services.reporting.domain.projection.ReportProjector;
import dtupay.services.reporting.domain.projection.ViewFactory;
import dtupay.services.reporting.domain.projection.views.CustomerView;
import dtupay.services.reporting.domain.projection.views.ManagerView;
import dtupay.services.reporting.domain.projection.views.MerchantView;
import dtupay.services.reporting.domain.repositories.LedgerRepository;
import dtupay.services.reporting.domain.repositories.ReadModelRepository;
import dtupay.services.reporting.utilities.Correlator;
import dtupay.services.reporting.utilities.EventTypes;
import io.cucumber.java.an.E;
import messaging.Event;
import messaging.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportingManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportingManager.class);
    private final ReadModelRepository readModelRepository;
    private final LedgerRepository ledgerRepository;
    private final ReportProjector reportProjector;
    private MessageQueue dtupayMQ;

    public ReportingManager(MessageQueue messageQueue, ReadModelRepository readRepository, LedgerRepository writeRepository) {
        this.readModelRepository = readRepository;
        this.ledgerRepository = writeRepository;
        this.dtupayMQ = messageQueue;

        this.reportProjector = new ReportProjector();

        this.dtupayMQ.addHandler(EventTypes.BANK_TRANSFER_CONFIRMED.getTopic(), this::handleBankTransferConfirmed);
        this.dtupayMQ.addHandler(EventTypes.CUSTOMER_REPORT_REQUESTED.getTopic(), this::handleCustomerReportRequested);
        this.dtupayMQ.addHandler(EventTypes.MERCHANT_REPORT_REQUESTED.getTopic(), this::handleMerchantReportRequested);
        this.dtupayMQ.addHandler(EventTypes.MANAGER_REPORT_REQUESTED.getTopic(), this::handleManagerReportRequested);

        Ledger initManager = Ledger.create("ADMIN", ReportingRole.MANAGER);
        ledgerRepository.save(initManager);
    }

    public void handleCustomerReportRequested(Event event) {
        logger.debug("Received CustomerReportRequested event: {}", event);
        var customerId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, Correlator.class);

        // generate the event with the report back to the facade
        ArrayList<CustomerView> views = new ArrayList<>();
        try {
            views = new ArrayList<>(getCustomerViews(customerId));
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        Event response = new Event(EventTypes.CUSTOMER_REPORT_GENERATED.getTopic(), views, correlationId);
        this.dtupayMQ.publish(response);
    }

    public void handleMerchantReportRequested(Event event) {
        logger.debug("Received MerchantReportRequested event: {}", event);
        var merchantId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, Correlator.class);

        // generate the event with the report back to the facade
        ArrayList<MerchantView> views = new ArrayList<>();
        try {
            views = new ArrayList<>(getMerchantViews(merchantId));
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        Event response = new Event(EventTypes.MERCHANT_REPORT_GENERATED.getTopic(), views, correlationId);
        this.dtupayMQ.publish(response);
    }

    public void handleManagerReportRequested(Event event) {
        logger.debug("Received ManagerReportRequested event: {}", event);
        var correlationId = event.getArgument(0, Correlator.class);

        // generate the event with the report back to the facade
        ArrayList<ManagerView> views = new ArrayList<>(getManagerViews());

        Event response = new Event(EventTypes.MANAGER_REPORT_GENERATED.getTopic(), views, correlationId);
        this.dtupayMQ.publish(response);
    }

    public void handleBankTransferConfirmed(Event event) {
        logger.debug("Received BankTransferConfirmed event: {}", event);
        var payLog = event.getArgument(0, PaymentRecord.class);


        // First time Customer, Merchant report creation
        var response1 = this.readModelRepository.contains(payLog.merchantId());
        if (!response1) {
            var id = createMerchantLedger(payLog);
        }
        logPayment(payLog.merchantId(), payLog);

        var response2 = this.readModelRepository.contains(payLog.customerId());
        if (!response2) {
            var id = createCustomerLedger(payLog);
        }
        logPayment(payLog.customerId(), payLog);

        logPayment("ADMIN", payLog);
    }

    /* Helper methods */
    public void logPayment(String ledgerId, PaymentRecord transaction) {
        updateLedger(ledgerId, Set.of(transaction));
    }

    /*____________________________________________________________________________________*/
    /* Command Operations */
    public String createCustomerLedger(PaymentRecord paymentRecord) {
        // Create a transaction ledger for the entity
        Ledger ledger = Ledger.create(paymentRecord.customerId(), ReportingRole.CUSTOMER);
        ledgerRepository.save(ledger);
        return ledger.getId();
    }

    public String createMerchantLedger(PaymentRecord paymentRecord) {
        // Create a transaction ledger for the entity
        Ledger ledger = Ledger.create(paymentRecord.merchantId(), ReportingRole.MERCHANT);
        ledgerRepository.save(ledger);
        return ledger.getId();
    }

    // < id --> Set<event>
    public void updateLedger(String ledgerId, Set<PaymentRecord> paymentRecords) {
        Ledger ledger = ledgerRepository.getById(ledgerId);
        ledger.update(paymentRecords);
        ledgerRepository.save(ledger);
    }

    /*_________________________________________________________________________*/
    /* Query Operations */
    public Set<CustomerView> getCustomerViews(String customerId) throws IllegalAccessException {
        if (ledgerRepository.getById(customerId).getRole() != ReportingRole.CUSTOMER) {
            throw new IllegalAccessException("Access control failure: accessing wrong ledger as a customer");
        }
        Set<PaymentRecord> transactions = readModelRepository.getTransactionsByLedger(customerId);
        return reportProjector.projectViews(transactions, ViewFactory::convertToCustomerView);
    }

    public Set<MerchantView> getMerchantViews(String merchantId) throws IllegalAccessException {
        if (ledgerRepository.getById(merchantId).getRole() != ReportingRole.MERCHANT) {
            throw new IllegalAccessException("Access control failure: accessing wrong ledger as a merchant");
        }
        Set<PaymentRecord> transactions = readModelRepository.getTransactionsByLedger(merchantId);
        return reportProjector.projectViews(transactions, ViewFactory::convertToMerchantView);
    }

    public Set<ManagerView> getManagerViews() {
        Set<PaymentRecord> transactions = readModelRepository.getAllTransactions();
        return reportProjector.projectViews(transactions, ViewFactory::convertToManagerView);
    }
}
    // aggregate/ User (aggregate) -> UserId (aggregate root)
    //                             -> Address (value objects)
    //                             -> Contact (value objects)

    // aggregate/ Ledger (aggregate) -> LedgerId (aggregate root)
    //                               -> PaymentRecord (value objects)

    // Report - customerId, token, amount, merchantId, customerBank, merchantBank, description
    // -> Token (aggregate root)
    // repo.save(report)
    // return report.getId() -> Token

    // manager: all fields -> Set<PaymentRecord>
    // customer: <amount, merchantId, token> with filter <customerId> on report
    // merchant: <amount, token> with filter <merchantId> on report
    // others: <customerId, customerBank, merchantBank, description>

    // Ledger (aggregate) -> LedgerId(token, aggregate root), Role, Set<PaymentRecord>
    // Report -> CustomerView, MerchantView, ManagerView