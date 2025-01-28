package dtupay.services.reporting.domain.aggregate.views;

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

	public ManagerView(String customerId, CustomerView customerView) {
		this.customerId = customerId;
		this.merchantId = customerView.getMerchantId();
		this.token = customerView.getToken();
		this.amount = customerView.getAmount();
	}
}
