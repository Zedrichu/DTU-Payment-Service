package dtupay.services.token.domain;

import dtupay.services.token.annotations.MethodAuthor;
import dtupay.services.token.domain.models.Token;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryTokenRepository implements TokenRepository {

	private Map<String, ArrayList<Token>> tokenLists = new ConcurrentHashMap<>();
	private Map<Token, String> customerIds = new ConcurrentHashMap<>();

	@MethodAuthor(author = "Paul Becker")
	@Override
	public synchronized int getNumberOfTokens(String customerId){
		return tokenLists.getOrDefault(customerId, new ArrayList<>()).size();
	}

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
	@Override
	public synchronized String extractId(Token token) {
		var cid = customerIds.get(token);
		if (cid == null) { return null;}
		customerIds.remove(token);
		tokenLists.get(cid).remove(token);
		return cid;
	}

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
	@Override
	public synchronized void removeId(String customerId) {
		ArrayList<Token> tokens = tokenLists.get(customerId);
		tokens.forEach(token -> customerIds.remove(token));
		tokenLists.remove(customerId);
	}

	@MethodAuthor(author = "Paul Becker")
	@Override
	public synchronized void addTokens(String customerId, ArrayList<Token> tokens) {
		if (!tokenLists.containsKey(customerId)) {
			tokenLists.put(customerId, new ArrayList<>());
		}
		tokenLists.get(customerId).addAll(tokens);

		tokens.forEach((token) -> customerIds.put(token, customerId));
	}
	
	@Override
	public synchronized boolean exists(String customerId) {
		return tokenLists.containsKey(customerId);
	}
}
