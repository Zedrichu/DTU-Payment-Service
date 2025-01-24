package dtupay.services.facade.domain.models.views;

import dtupay.services.facade.domain.models.Token;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public abstract class LogView implements Serializable {
	int amount;
	Token token;

	LogView(int amount, Token token) {}
}
