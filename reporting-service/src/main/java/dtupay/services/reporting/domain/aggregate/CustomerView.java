package dtupay.services.reporting.domain.aggregate;

import dtupay.services.reporting.domain.models.Token;
import lombok.Value;

@Value
public class CustomerView {
	private static final long serialVersionUID = 1231553453445L;
	int amount;
	String merchantId;
	Token token;
}
