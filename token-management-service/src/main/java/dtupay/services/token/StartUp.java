package dtupay.services.token;

import dtupay.services.token.domain.MemoryTokenRepository;
import dtupay.services.token.domain.TokenManager;
import dtupay.services.token.domain.TokenRepository;
import messaging.implementations.RabbitMqQueue;

public class StartUp {
	private final String HOSTNAME = "rabbitMq";

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
