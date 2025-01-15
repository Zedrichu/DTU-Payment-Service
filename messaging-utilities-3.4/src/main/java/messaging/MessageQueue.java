package messaging;

import java.util.function.Consumer;

public interface MessageQueue {

	void publish(Event event);
	void addHandler(String topic, Consumer<Event> handler);

}
