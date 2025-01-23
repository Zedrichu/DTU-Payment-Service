package dtupay.services.token.domain.models;

import dtupay.services.token.annotations.ClassAuthor;
import dtupay.services.token.annotations.MethodAuthor;
import dtupay.services.token.utilities.Correlator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

@ClassAuthor(author = "Paul Becker", stdno = "")
@Getter(onMethod_={@Synchronized})
@Setter(onMethod_={@Synchronized})
@EqualsAndHashCode
public class TokenGenerationAggregate {

	private Correlator correlator;

	private boolean requestReceived;

	private int noToken = 0;

	private String customerId;

	private boolean customerVerified;

	private boolean customerHandled;
	public TokenGenerationAggregate(Correlator correlator) {
		this.correlator = correlator;
	}

	@MethodAuthor(author = "Adrian Zvizdenco", stdno = "s204683")
	public synchronized boolean isComplete(){
		return requestReceived && customerId != null && customerHandled;
	}
}
