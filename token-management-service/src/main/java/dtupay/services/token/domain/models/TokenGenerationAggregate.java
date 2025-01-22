package dtupay.services.token.domain.models;

import dtupay.services.token.utilities.Correlator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode
public class TokenGenerationAggregate {

	private Correlator correlator;
	@Setter
	private boolean requestReceived;
	@Setter
	private int noToken = 0;
	@Setter
	private String customerId;
	@Setter
	private boolean customerVerified;
	@Setter
	private boolean customerHandled;
	public TokenGenerationAggregate(Correlator correlator) {
		this.correlator = correlator;
	}

	public boolean isComplete(){
		return requestReceived && customerId != null && customerHandled;
	}
}
