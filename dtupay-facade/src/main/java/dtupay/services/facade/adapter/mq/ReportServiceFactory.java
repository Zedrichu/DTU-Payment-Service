package dtupay.services.facade.adapter.mq;

import dtupay.services.facade.domain.ReportService;
import messaging.implementations.RabbitMqQueue;

import dtupay.services.facade.annotations.ClassAuthor;

@ClassAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
public class ReportServiceFactory {
	static ReportService service = null;

	public synchronized ReportService getService() {
		// The singleton pattern.
		// Ensure that there is at most one instance of a MerchantService
		if (service != null) {
			return service;
		}

		// Hookup the classes to send and receive
		// messages via RabbitMq, i.e. RabbitMqSender and
		// RabbitMqListener.
		var mq = new RabbitMqQueue("rabbitMq");
		service = new ReportService(mq);
		return service;
	}
}
