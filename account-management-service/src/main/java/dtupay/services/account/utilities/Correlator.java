package dtupay.services.account.utilities;

import lombok.Value;

import java.util.UUID;

@Value
public class Correlator {

	public UUID getId() {
		return id;
	}

	UUID id;

	public Correlator(UUID id) { this.id = id; }

	public static Correlator random() { return new Correlator(UUID.randomUUID()); }
}
