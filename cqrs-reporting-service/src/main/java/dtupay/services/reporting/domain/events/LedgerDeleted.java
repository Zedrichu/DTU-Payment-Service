package dtupay.services.reporting.domain.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serial;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LedgerDeleted extends Event {
	@Serial
	private static final long serialVersionUID = 21312573441240574L;

	String id;
}