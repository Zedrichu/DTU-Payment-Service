package dtupay.services.reporting.domain.aggregate.views;

import dtupay.services.reporting.domain.models.Token;
import lombok.Value;

import java.io.Serial;
import java.io.Serializable;

@Value
public class MerchantView implements Serializable {
	@Serial
	private static final long serialVersionUID = 1231553453445L;
	int amount;
	Token token;
}
