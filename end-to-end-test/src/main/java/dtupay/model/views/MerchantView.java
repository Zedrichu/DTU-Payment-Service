package dtupay.model.views;

import dtupay.model.Token;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class MerchantView extends LogView {
	public MerchantView() {}

	public MerchantView(Token token, int amount) {
		super(amount, token);
	}
}
