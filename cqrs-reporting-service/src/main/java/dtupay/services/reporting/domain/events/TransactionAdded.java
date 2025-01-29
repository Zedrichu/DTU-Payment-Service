package dtupay.services.reporting.domain.events;

import dtupay.services.reporting.domain.models.PaymentRecord;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serial;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionAdded extends Event {
	@Serial
	private static final long serialVersionUID = 21312521834438574L;

	String id;
	PaymentRecord transaction;
}
