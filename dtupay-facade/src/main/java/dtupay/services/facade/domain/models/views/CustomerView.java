package dtupay.services.facade.domain.models.views;

import dtupay.services.facade.domain.models.Token;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class CustomerView extends LogView {
	String merchantId;

	public CustomerView(String merchantId, Token token, int amount) {
		super(amount, token);
		this.merchantId = merchantId;
	}
}
