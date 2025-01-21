package dtupay.services.token.domain.models;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryTokenRepository implements TokenRepository {

	private Map<String, ArrayList<Token>> tokenLists = new ConcurrentHashMap<>();
	private Map<Token, String> customerIds = new ConcurrentHashMap<>();

	@Override
	public synchronized int getNumberOfTokens(String customerId){
		return tokenLists.getOrDefault(customerId, new ArrayList<>()).size();
	}

	@Override
	public synchronized String extractId(Token token) {
		var cid = customerIds.get(token);
		if (cid == null) { return null;}
		customerIds.remove(token);
		tokenLists.get(cid).remove(token);
		return cid;
	}

	@Override
	public synchronized void removeId(String customerId) {
		ArrayList<Token> tokens = tokenLists.get(customerId);
		tokens.forEach(token -> customerIds.remove(token));
		tokenLists.remove(customerId);
	}

	@Override
	public synchronized void addTokens(String customerId, ArrayList<Token> tokens) {
		if (!tokenLists.containsKey(customerId)) {
			tokenLists.put(customerId, new ArrayList<>());
		}
		tokenLists.get(customerId).addAll(tokens);

		tokens.forEach((token) -> customerIds.put(token, customerId));
	}
}
