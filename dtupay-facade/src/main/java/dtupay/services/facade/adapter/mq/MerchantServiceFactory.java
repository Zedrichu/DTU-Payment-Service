package dtupay.services.facade.adapter.mq;

import dtupay.services.facade.domain.MerchantService;
import messaging.implementations.RabbitMqQueue;

public class MerchantServiceFactory {
	static MerchantService service = null;

	public synchronized MerchantService getService() {
		// The singleton pattern.
		// Ensure that there is at most one instance of a MerchantService
		if (service != null) {
			return service;
		}

		// Hookup the classes to send and receive
		// messages via RabbitMq, i.e. RabbitMqSender and
		// RabbitMqListener.
		var mq = new RabbitMqQueue("rabbitMq");
		service = new MerchantService(mq);
		return service;
	}
}
