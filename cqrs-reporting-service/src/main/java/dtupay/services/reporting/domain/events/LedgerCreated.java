package dtupay.services.reporting.domain.events;

import dtupay.services.reporting.domain.aggregate.ReportingRole;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serial;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LedgerCreated extends Event {
	@Serial
	private static final long serialVersionUID = 213125734438574L;

	String id;
	ReportingRole role;

}