package dtupay.services.token.utilities;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
public class Correlator {

	UUID id;

	public Correlator(UUID id) { this.id = id; }

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

	public static Correlator random() { return new Correlator(UUID.randomUUID()); }

	@Override
	public String toString() {
		return "Correlator{" + id +'}';
	}
}
