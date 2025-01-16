package dtupay.facade.adapter.rest;

import dtupay.facade.domain.CustomerService;
import messaging.implementations.RabbitMqQueue;

public class CustomerFactory {

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
		var mq = new RabbitMqQueue("localhost");
		service = new CustomerService(mq);
		return service;
	}
}
