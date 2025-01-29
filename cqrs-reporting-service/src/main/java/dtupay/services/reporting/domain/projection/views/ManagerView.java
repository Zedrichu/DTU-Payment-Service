package dtupay.services.reporting.domain.projection.views;

import dtupay.services.reporting.domain.models.Token;
import lombok.Value;

import java.io.Serial;
import java.io.Serializable;

@Value
public class ManagerView implements Serializable {
	@Serial
	private static final long serialVersionUID = 1231553453445L;
	int amount;
	String merchantId;
	Token token;
	String customerId;

	public ManagerView(String customerId, String merchantId, Token token, int amount) {
		this.customerId = customerId;
		this.merchantId = merchantId;
		this.token = token;
		this.amount = amount;
	}
}
