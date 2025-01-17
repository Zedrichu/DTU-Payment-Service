package dtupay.services.facade.exception;

public class AccountCreationException extends RuntimeException {

	public AccountCreationException() {}

	public AccountCreationException(String message) {
		super(message);
	}
}
