package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.aggregate.CustomerReport;
import messaging.Event;
import messaging.MessageQueue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentLogRepository {
    private EventStore eventStore;
    private Map<String, List<Event>> store = new ConcurrentHashMap<>();

    public PaymentLogRepository(MessageQueue bus) {
        eventStore = new EventStore(bus);
    }


    public void save(CustomerReport customerReport) {
        eventStore.addEvents(customerReport.getCId(), customerReport.getAppliedEvents());
        customerReport.clearAppliedEvents();
    }

}
