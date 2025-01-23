package dtupay.services.reporting.domain.models.views;

import dtupay.services.reporting.domain.models.Token;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
public class CustomerView extends LogView {
	String merchantId;

	public CustomerView(String merchantId, Token token, int amount) {
		super(amount, token);
		this.merchantId = merchantId;
	}
}
