package dtupay.services.reporting.domain.aggregate.views;

import dtupay.services.reporting.domain.models.Token;
import lombok.Value;

@Value
public class MerchantView {
	private static final long serialVersionUID = 1231553453445L;
	int amount;
	Token token;
}
