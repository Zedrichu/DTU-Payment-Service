package dtupay.services.account;

import dtupay.services.account.domain.AccountRepository;
import dtupay.services.account.domain.MemoryAccountRepository;
import dtupay.services.account.domain.models.Customer;
import dtupay.services.account.domain.models.Merchant;
import dtupay.services.account.domain.models.Wither;
import messaging.implementations.RabbitMqQueue;

public class StartUp {
	private String HOSTNAME = "rabbitMq";

	public static void main(String[] args) throws Exception {
		new StartUp().startUp();
	}

	private void startUp() throws Exception {
		System.out.println(HOSTNAME);
		var mq = new RabbitMqQueue(HOSTNAME);
		AccountRepository<Customer> customerAccountRepository = new MemoryAccountRepository<>();
		AccountRepository<Merchant> merchantAccountRepository = new MemoryAccountRepository<>();

		new AccountManager(mq, customerAccountRepository, merchantAccountRepository);
	}
}
