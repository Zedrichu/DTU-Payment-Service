package dtupay.services.facade.exception;

public class AccountDeletionException extends RuntimeException {

    public AccountDeletionException() {}

    public AccountDeletionException(String message) {
        super(message);
    }
}