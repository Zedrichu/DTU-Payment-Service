package dtupay.services.account.domain;

public interface AccountRepository<T> {

	String createAccount(T user);
	T getAccount(String accountId);
	void removeAccount(String accountId);
	boolean exists(String accountId);
}
