package dtupay.services.facade.domain.models.views;

import dtupay.services.facade.domain.models.Token;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ManagerView extends CustomerView {
	String customerId;

	public ManagerView(String customerId, String merchantId, Token token, int amount) {
		super(merchantId, token, amount);
		this.customerId = customerId;
	}
}

