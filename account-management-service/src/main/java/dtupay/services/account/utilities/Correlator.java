package dtupay.services.account.utilities;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
@EqualsAndHashCode
public class Correlator {

	public UUID getId() {
		return id;
	}

	UUID id;

	public Correlator(UUID id) { this.id = id; }

	public static Correlator random() { return new Correlator(UUID.randomUUID()); }

	@Override
	public String toString() {
		return "Correlator{" + id +'}';
	}
}
