package dtupay.model;

import dtupay.model.views.LogView;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public class Report<T extends LogView> implements Serializable {
	private UUID id;
	private ArrayList<T> entries;

	public Report(ArrayList<T> entries) {
		this.id = UUID.randomUUID();
		this.entries = entries;
	}
}
