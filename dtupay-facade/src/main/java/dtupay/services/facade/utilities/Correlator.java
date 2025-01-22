package dtupay.services.facade.utilities;


import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
public class Correlator {

	public UUID getId() {
		return id;
	}

	UUID id;

	public Correlator(UUID id) { this.id = id; }

	public static Correlator random() { return new Correlator(UUID.randomUUID()); }

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Correlator that = (Correlator) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "Correlator {" + id + '}';
	}
}
