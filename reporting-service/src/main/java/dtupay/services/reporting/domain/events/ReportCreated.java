package dtupay.services.reporting.domain.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serial;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReportCreated extends Event {
	@Serial
	private static final long serialVersionUID = 213125734438574L;

	String reportId;
	String role;
}