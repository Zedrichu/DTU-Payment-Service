package dtupay.services.reporting.config;

import dtupay.services.reporting.adapters.persistence.LedgerWriteRepository;
import dtupay.services.reporting.application.services.ReportingManager;
import dtupay.services.reporting.query.projection.LedgerViewProjector;
import dtupay.services.reporting.query.repositories.LedgerReadRepository;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueAsync;
import messaging.implementations.RabbitMqQueue;

public class StartUp {

	public final String HOSTNAME = "rabbitMq";

  public static void main(String[] args) {
		new StartUp().startUp();
	}

	private void startUp() {
    System.out.println(HOSTNAME);
		var externalMQ = new RabbitMqQueue(HOSTNAME);

		var intraMQ = new MessageQueueAsync();

		LedgerWriteRepository writeRepository = new LedgerWriteRepository(intraMQ);
		LedgerReadRepository readRepository = new LedgerReadRepository();

		new LedgerViewProjector(writeRepository, readRepository, intraMQ);

		new ReportingManager(externalMQ, readRepository, writeRepository);
	}
}
