package dtupay.model.views;

import dtupay.model.Token;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CustomerView extends LogView {
	String merchantId;

	public CustomerView() {
		super();
	}

	public CustomerView(String merchantId, Token token, int amount) {
		super(amount, token);
		this.merchantId = merchantId;
	}
}
