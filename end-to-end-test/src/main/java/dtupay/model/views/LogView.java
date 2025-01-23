package dtupay.model.views;

import dtupay.model.Token;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@EqualsAndHashCode
@Getter
public abstract class LogView implements Serializable {
	@JsonbProperty("amount")
	int amount;
	@JsonbProperty("token")
	Token token;

	LogView() {}

	LogView(int amount, Token token) {
		this.amount = amount;
		this.token = token;
	}
}
