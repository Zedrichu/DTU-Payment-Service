package service.behaviour.tests;

import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class MockMessageQueue implements MessageQueue {

	private final Function<Event, String> idExtractor;
	private final Map<String, CompletableFuture<Event>> publishedEvents;

	public MockMessageQueue(Function<Event, String> mapping,  Map<String, CompletableFuture<Event>> eventMap) {
		this.idExtractor = mapping;
		this.publishedEvents = eventMap;
	}

	@Override
	public void publish(Event event) {
		String id = idExtractor.apply(event);
		if (publishedEvents.containsKey(id)) {
			publishedEvents.get(id).complete(event);
		}
	}

	@Override
	public void addHandler(String topic, Consumer<Event> handler) {}
}
