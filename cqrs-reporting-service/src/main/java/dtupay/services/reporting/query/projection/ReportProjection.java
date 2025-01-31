package dtupay.services.reporting.query.projection;

import dtupay.services.reporting.query.repositories.LedgerReadRepository;
import dtupay.services.reporting.query.views.CustomerView;
import dtupay.services.reporting.query.views.ManagerView;
import dtupay.services.reporting.query.views.MerchantView;

import java.util.Set;

/* projection: query -> projection handler -> view-based report */
public class ReportProjection {
	private final LedgerReadRepository repository;

	public ReportProjection(LedgerReadRepository readRepository) {
		this.repository = readRepository;
	}

	public Set<CustomerView> getCustomerViews(String customerId) {
		return repository.getCustomerViewsByLedger(customerId);
	}

	public Set<MerchantView> getMerchantViews(String merchantId) {
		return repository.getMerchantViewsByLedger(merchantId);
	}

	public Set<ManagerView> getManagerViews() {
		return repository.getManagerViews();
	}

	public boolean contains(String id) {
		return repository.contains(id);
	}
}
