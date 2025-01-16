package dtupay.services.account;

import messaging.implementations.RabbitMqQueue;

public class StartUp {
	private String HOSTNAME = "rabbitMq";

	public static void main(String[] args) throws Exception {
		new StartUp().startUp();
	}

	private void startUp() throws Exception {
		System.out.println(HOSTNAME);
		var mq = new RabbitMqQueue(HOSTNAME);
		new AccountManagementService(mq);
	}
}
