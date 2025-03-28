package dtupay.services.reporting.domain.models.views;

import dtupay.services.reporting.domain.models.Token;
import lombok.*;

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

