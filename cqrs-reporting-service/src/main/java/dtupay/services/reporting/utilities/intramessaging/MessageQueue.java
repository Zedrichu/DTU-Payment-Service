package dtupay.services.reporting.utilities.intramessaging;

import java.util.function.Consumer;

public interface MessageQueue {

	void publish(Message message);
	void addHandler(Class<? extends Message> event, Consumer<Message> handler);

}
