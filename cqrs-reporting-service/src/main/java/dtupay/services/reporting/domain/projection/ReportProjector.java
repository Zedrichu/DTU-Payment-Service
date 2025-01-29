package dtupay.services.reporting.domain.projection;

import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.projection.views.CustomerView;
import dtupay.services.reporting.domain.projection.views.ManagerView;
import dtupay.services.reporting.domain.projection.views.MerchantView;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReportProjector {

	public <T> Set<T> projectViews(Set<PaymentRecord> transactions, Function<PaymentRecord, T> projector) {
		return transactions.stream().map(projector).collect(Collectors.toSet());
	}

}
