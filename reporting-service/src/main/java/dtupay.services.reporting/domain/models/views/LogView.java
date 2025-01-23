package dtupay.services.reporting.domain.models.views;

import dtupay.services.reporting.domain.models.Token;

import java.io.Serializable;

public abstract class LogView implements Serializable {
	int amount;
	Token token;

	LogView(int amount, Token token) {}
}
