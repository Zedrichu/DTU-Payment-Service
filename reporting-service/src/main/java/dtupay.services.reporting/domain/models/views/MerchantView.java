package dtupay.services.reporting.domain.models.views;

import dtupay.services.reporting.domain.models.Token;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class MerchantView extends LogView {
	public MerchantView(Token token, int amount) {
		super(amount, token);
	}
}
