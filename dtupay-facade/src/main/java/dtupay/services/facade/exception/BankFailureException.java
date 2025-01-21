package dtupay.services.facade.exception;

public class BankFailureException extends RuntimeException {
	public BankFailureException(String message) {
		super(message);
	}
}
