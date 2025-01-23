package dtupay.services.facade.adapter.mq;

import dtupay.services.facade.annotations.ClassAuthor;
import dtupay.services.facade.domain.CustomerService;
import messaging.implementations.RabbitMqQueue;

@ClassAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
public class CustomerServiceFactory {

	static CustomerService service = null;

	public synchronized CustomerService getService() {
		// The singleton pattern.
		// Ensure that there is at most one instance of a CustomerService
		if (service != null) {
			return service;
		}

		// Hookup the classes to send and receive
		// messages via RabbitMq, i.e. RabbitMqSender and
		// RabbitMqListener.
		var mq = new RabbitMqQueue("rabbitMq");
		service = new CustomerService(mq);
		return service;
	}
}
