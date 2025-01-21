package dtupay.services.token.domain.models;

import dtupay.services.token.annotations.ClassAuthor;

import java.util.ArrayList;

@ClassAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
public interface TokenRepository {

	int getNumberOfTokens(String id);
	String extractId(Token token);
	void removeId(String id);
	void addTokens(String id, ArrayList<Token> tokens);
	boolean exists(String id);
}
