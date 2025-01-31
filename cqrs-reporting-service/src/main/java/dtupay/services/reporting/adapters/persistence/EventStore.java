package dtupay.services.reporting.adapters.persistence;

import dtupay.services.reporting.domain.events.Event;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class EventStore {

    private final Map<String, List<Event>> store = new ConcurrentHashMap<>();

    private final MessageQueue eventBus;
    public EventStore(MessageQueue bus) { eventBus = bus;}


    public void addEvent(String payId, Event event) {
        if (!store.containsKey(payId)) {
            store.put(payId, new ArrayList<>());
        }
        store.get(payId).add(event);
        eventBus.publish(event);
    }

    public Stream<Event> getEventsFor(String repId) {
        if (!store.containsKey(repId)) {
            store.put(repId, new ArrayList<>());
        }
        return store.get(repId).stream();
    }

    public void addEvents(@NonNull String reportId, List<Event> appliedEvents) {
        appliedEvents.forEach(e -> addEvent(reportId, e));
    }
}
