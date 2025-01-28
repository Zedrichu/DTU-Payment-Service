package dtupay.services.reporting.domain.aggregate;

import dtupay.services.reporting.domain.aggregate.views.CustomerView;
import dtupay.services.reporting.domain.aggregate.views.MerchantView;
import dtupay.services.reporting.domain.events.CustomerViewAdded;
import dtupay.services.reporting.domain.events.Event;
import dtupay.services.reporting.domain.events.MerchantViewAdded;
import dtupay.services.reporting.domain.events.ReportCreated;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.repositories.ReportRepository;
import dtupay.services.reporting.utilities.intramessaging.Message;

import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class PaymentReport {
    private String reportId;
    private String role;

    private final Set<CustomerView> customerViews = new HashSet<>();
    private final Set<MerchantView> merchantViews = new HashSet<>();

    private final List<Event> appliedEvents = new ArrayList<Event>();

    private final Map<Class<? extends Message>, Consumer<Message>> handlers = new HashMap<>();

    public static PaymentReport createManager() {
        var reportId = "admin";
        ReportCreated event = new ReportCreated(reportId, "manager");

        var report = new PaymentReport();
        report.reportId = "admin";
        report.role = "manager";

        report.appliedEvents.add(event);
        return report;
    }

    public static PaymentReport createMerchant(PaymentRecord paymentRecord) {
        var reportId = paymentRecord.merchantId();
        ReportCreated event = new ReportCreated(reportId, "merchant");

        var merchantReport = new PaymentReport();
        merchantReport.reportId = reportId;
        merchantReport.role = "merchant";

        merchantReport.appliedEvents.add(event);
        return merchantReport;
    }

    public static PaymentReport createCustomer(PaymentRecord paymentRecord) {
        var reportId = paymentRecord.customerId();
        ReportCreated event = new ReportCreated(reportId, "customer");

        var customerReport = new PaymentReport();
        customerReport.reportId = reportId;
        customerReport.role = "customer";

        customerReport.appliedEvents.add(event);
        return customerReport;
    }

    // cid -> arrayList ==> cid --> Set<CustomerViews>
    public static PaymentReport createFromEvents(Stream<Event> events) {
        PaymentReport report = new PaymentReport();
        report.applyEvents(events);
        return report;
    }

    public PaymentReport() {
        registerEventHandlers();
    }

    private void registerEventHandlers() {
        handlers.put(CustomerViewAdded.class, e -> apply((CustomerViewAdded) e));
        handlers.put(MerchantViewAdded.class, e -> apply((MerchantViewAdded) e));
        handlers.put(ReportCreated.class, e -> apply((ReportCreated) e));
    }

    public void update(Set<CustomerView> customerViews, Set<MerchantView> merchantViews) {
        addNewCustomerViews(customerViews);
        addNewMerchantViews(merchantViews);
        applyEvents(appliedEvents.stream());
    }

    private void addNewCustomerViews(Set<CustomerView> customerViews){
        var events = customerViews.stream().filter(a -> !getCustomerViews().contains(a))
              .map(customerView -> (Event) new CustomerViewAdded(reportId, customerView.getAmount(),
                    customerView.getToken(), customerView.getMerchantId()))
              .toList();
        appliedEvents.addAll(events);
    }

    private void addNewMerchantViews(Set<MerchantView> merchantViews) {
        var events = merchantViews.stream().filter(mw -> !getMerchantViews().contains(mw))
              .map(mView -> (Event) new MerchantViewAdded(reportId, mView.getAmount(), mView.getToken()))
              .toList();
        appliedEvents.addAll(events);
    }


    /* Event Handling */

    public void applyEvents(Stream<Event> events) {
        events.forEachOrdered(this::applyEvent);
        if (this.getReportId() == null) {
            throw new Error("Report ID does not exist");
        }
    }

    private void applyEvent(Event e) {
        handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
    }

    private void missingHandler(Message e) {
        throw new Error("handler for event "+e+" missing");
    }

    private void apply(ReportCreated event) {
        reportId = event.getReportId();
        role = event.getRole();
    }

    private void apply(CustomerViewAdded event) {
        var customerView = new CustomerView(event.getAmount(), event.getMerchantId(), event.getToken());
        this.customerViews.add(customerView);
    }

    private void apply(MerchantViewAdded event) {
        var merchantView = new MerchantView(event.getAmount(), event.getToken());
        this.merchantViews.add(merchantView);
    }

    public void clearAppliedEvents() { appliedEvents.clear();
    }
}
