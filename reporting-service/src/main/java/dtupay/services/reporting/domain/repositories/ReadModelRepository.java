package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.aggregate.CustomerView;
import dtupay.services.reporting.domain.aggregate.MerchantView;
import dtupay.services.reporting.domain.events.CustomerViewAdded;
import dtupay.services.reporting.domain.events.MerchantViewAdded;
import dtupay.services.reporting.domain.events.ReportCreated;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;

import java.util.*;

public class ReadModelRepository {

    private Map<String, Set<CustomerView>> customerViews = new HashMap<>();

    private Map<String, Set<MerchantView>> merchantViews = new HashMap<>();

    public ReadModelRepository(MessageQueue eventQueue) {
        eventQueue.addHandler(ReportCreated.class, e -> apply((ReportCreated) e));
        eventQueue.addHandler(CustomerViewAdded.class, e -> apply((CustomerViewAdded) e));
        eventQueue.addHandler(MerchantViewAdded.class, e -> apply((MerchantViewAdded) e));
    }

    public AbstractMap.SimpleEntry<Boolean, String> contains(String reportId) {
        String stack = customerViews.containsKey(reportId) ? "customer" : null;
        stack = merchantViews.containsKey(reportId) && stack == null ? "merchant" : null;
        if (stack != null) {
            return new AbstractMap.SimpleEntry<>(true, stack);
        }
        return new AbstractMap.SimpleEntry<>(false, null);

    }

    public void apply(ReportCreated event) {
    }

    public void apply(CustomerViewAdded event) {
        var customerViewsByReport = customerViews.getOrDefault(event.getReportId(), new HashSet<>());
        customerViewsByReport.add(new CustomerView(event.getAmount(), event.getMerchantId(), event.getToken()));
        customerViews.put(event.getReportId(), customerViewsByReport);
    }

    public void apply(MerchantViewAdded event) {
        var merchantViewsByReport = merchantViews.getOrDefault(event.getReportId(), new HashSet<>());
        merchantViewsByReport.add(new MerchantView(event.getAmount(),event.getToken()));
        merchantViews.put(event.getReportId(), merchantViewsByReport);
    }
}
