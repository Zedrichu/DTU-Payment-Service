package dtupay.services.reporting.domain.models.views;


import dtupay.services.reporting.domain.models.Token;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ReportView {
	String customerId;
	String merchantId;
	Token token;
	int amount;
}
