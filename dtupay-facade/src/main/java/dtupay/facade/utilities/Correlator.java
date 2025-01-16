package dtupay.facade.utilities;

import lombok.Value;

import java.util.UUID;

@Value
public class Correlator {

	private UUID id;

	public Correlator(UUID id) { this.id = id; }

	public static Correlator random() { return new Correlator(UUID.randomUUID()); }
}
