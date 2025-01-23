package dtupay.services.facade.domain.models;

import dtupay.services.facade.domain.models.views.LogView;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
public class Report<T extends LogView>{
	private UUID id;
	private ArrayList<T> entries;

	public Report(ArrayList<T> entries) {
		this.id = UUID.randomUUID();
		this.entries = entries;
	}

}
