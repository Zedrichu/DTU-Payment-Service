package dtupay.model.views;

import dtupay.model.Token;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@EqualsAndHashCode(callSuper = true)
@Getter
public class ManagerView extends CustomerView {
	String customerId;

	public ManagerView() {
		super();
	}

	public ManagerView(String customerId, String merchantId, Token token, int amount) {
		super(merchantId, token, amount);
		this.customerId = customerId;
	}
}

