package dtupay.services.facade.domain.models.views;

import dtupay.services.facade.domain.models.Token;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class MerchantView extends LogView {
	public MerchantView(Token token, int amount) {
		super(amount, token);
	}
}
