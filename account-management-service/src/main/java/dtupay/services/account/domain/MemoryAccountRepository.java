package dtupay.services.account.domain;

import dtupay.services.account.annotations.ClassAuthor;
import dtupay.services.account.domain.models.Wither;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ClassAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
public class MemoryAccountRepository<T extends Wither<T>> implements AccountRepository<T> {
	private final Map<String, T> accounts = new ConcurrentHashMap<>();

	@Override
	public String createAccount(T user) {
		var id = UUID.randomUUID().toString();
		user = (T) user.withId(id);
		accounts.put(id, user);
		return id;
	}

	@Override
	public T getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void removeAccount(String accountId) {
		accounts.remove(accountId);
	}

	@Override
	public boolean exists(String accountId) {
		return accounts.containsKey(accountId);
	}
}
