package dtupay.services.token;

import dtupay.services.token.domain.models.MemoryTokenRepository;
import dtupay.services.token.domain.models.TokenManager;
import dtupay.services.token.domain.models.TokenRepository;
import messaging.implementations.RabbitMqQueue;

public class StartUp {
	private String HOSTNAME = "rabbitMq";

	public static void main(String[] args) throws Exception {
		new StartUp().startUp();
	}

	private void startUp() throws Exception {
		System.out.println(HOSTNAME);
		var mq = new RabbitMqQueue(HOSTNAME);
		TokenRepository tokenRepository = new MemoryTokenRepository();
		new TokenManager(mq, tokenRepository);
	}
}
