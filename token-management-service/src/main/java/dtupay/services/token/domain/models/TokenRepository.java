package dtupay.services.token.domain.models;

import java.util.ArrayList;

public interface TokenRepository {

	int getNumberOfTokens(String id);
	String extractId(Token token);
	void removeId(String id);
	void addTokens(String id, ArrayList<Token> tokens);
	boolean exists(String id);
}
