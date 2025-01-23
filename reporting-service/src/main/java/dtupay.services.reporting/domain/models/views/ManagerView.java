package dtupay.services.reporting.domain.models.views;

import dtupay.services.reporting.domain.models.Token;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class ManagerView extends CustomerView {
	public String customerId;
	public String description;

	public ManagerView(String customerId, String description, Token token, String merchantId, int amount) {
		super(merchantId, token, amount);
		this.customerId = customerId;
		this.description = description;
	}
}
