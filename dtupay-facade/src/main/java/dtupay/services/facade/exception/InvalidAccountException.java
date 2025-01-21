package dtupay.services.facade.exception;

public class InvalidAccountException extends RuntimeException {
    public InvalidAccountException() {}

    public InvalidAccountException(String message) {
        super(message);
    }
}
