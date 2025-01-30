package dtupay.services.reporting;

import dtupay.services.reporting.domain.ReportingManager;
import dtupay.services.reporting.domain.repositories.LedgerWriteRepository;
import dtupay.services.reporting.domain.repositories.LedgerReadRepository;
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
		var readModelRepository = new LedgerReadRepository(intraMQ);
		var reportRepository = new LedgerWriteRepository(intraMQ);

		new ReportingManager(mq, readModelRepository, reportRepository);
	}
}
