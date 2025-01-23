package dtupay.services.reporting.domain.models;


import dtupay.services.reporting.domain.models.views.LogView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Report {
	private UUID id;
	private List<LogView> entries;

	public Report(List<LogView> entries) {
		this.id = UUID.randomUUID();
		this.entries = entries;
	}

}
