package dtupay.services.reporting.domain.repositories;

import messaging.Event;
import messaging.MessageQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class EventStore {
    private Map<String, List<Event>> store = new ConcurrentHashMap<>();

    private MessageQueue eventBus;
    public EventStore(MessageQueue bus) { eventBus = bus;}


    public void addEvent(String payId, Event event) {
        if (!store.containsKey(payId)) {
            store.put(payId, new ArrayList<>());
        }
        store.get(payId).add(event);
        eventBus.publish(event);
    }

    public void addEvents(String payId, List<Event> appliedEvents) {
        appliedEvents.stream().forEach(e -> addEvent(payId, e));

    }
}
