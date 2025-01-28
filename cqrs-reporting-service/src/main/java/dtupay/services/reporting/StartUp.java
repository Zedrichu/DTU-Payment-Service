package dtupay.services.reporting;

import dtupay.services.reporting.domain.ReportingManager;
import dtupay.services.reporting.domain.repositories.ReadModelRepository;
import dtupay.services.reporting.domain.repositories.ReportRepository;
import messaging.implementations.RabbitMqQueue;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueAsync;

public class StartUp {
	private String HOSTNAME = "rabbitMq";

	public static void main(String[] args) throws Exception {
		new StartUp().startUp();
	}

	private void startUp() throws Exception {
		System.out.println(HOSTNAME);
		var mq = new RabbitMqQueue(HOSTNAME);

		var intraMQ = new MessageQueueAsync();
		var readModelRepository = new ReadModelRepository(intraMQ);
		var reportRepository = new ReportRepository(intraMQ);

		new ReportingManager(mq, readModelRepository, reportRepository);
	}
}
