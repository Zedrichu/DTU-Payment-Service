package dtupay.exceptions;

import dtupay.annotations.ClassAuthor;

@ClassAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
public class AccountCreationException extends RuntimeException {

	public AccountCreationException(String message) {
		super(message);
	}
}
