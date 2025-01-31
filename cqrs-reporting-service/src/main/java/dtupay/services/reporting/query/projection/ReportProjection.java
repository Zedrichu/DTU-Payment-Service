package dtupay.services.reporting.query.projection;

import dtupay.services.reporting.domain.entities.ReportingRole;
import dtupay.services.reporting.models.PaymentRecord;
import dtupay.services.reporting.query.views.CustomerView;
import dtupay.services.reporting.query.views.ManagerView;
import dtupay.services.reporting.query.views.MerchantView;
import dtupay.services.reporting.query.repositories.LedgerReadRepository;
import dtupay.services.reporting.adapters.persistence.LedgerWriteRepository;
import org.picocontainer.annotations.Inject;

import java.util.Set;

public class ReportProjection {
	private final LedgerReadRepository repository;
	private final LedgerWriteRepository writeRepository;
	@Inject
	private final LedgerViewProjector ledgerViewProjector;

	public ReportProjection(LedgerReadRepository readRepository, LedgerWriteRepository writeRepository) {
		this.repository = readRepository;
		this.writeRepository = writeRepository;

		this.ledgerViewProjector = new LedgerViewProjector(readRepository);
	}

	public Set<CustomerView> getCustomerViewsById(String customerId) throws IllegalAccessException {
		if (writeRepository.getById(customerId).getRole() != ReportingRole.CUSTOMER) {
			throw new IllegalAccessException("Access control failure: accessing wrong ledger as a customer");
		}
		Set<PaymentRecord> transactions = repository.getTransactionsByLedger(customerId);
		return ledgerViewProjector.projectViews(transactions, ViewFactory::convertToCustomerView);
	}

	public Set<MerchantView> getMerchantViews(String merchantId) throws IllegalAccessException {
		if (writeRepository.getById(merchantId).getRole() != ReportingRole.MERCHANT) {
			throw new IllegalAccessException("Access control failure: accessing wrong ledger as a merchant");
		}
		Set<PaymentRecord> transactions = repository.getTransactionsByLedger(merchantId);
		return ledgerViewProjector.projectViews(transactions, ViewFactory::convertToMerchantView);
	}

	public Set<ManagerView> getManagerViews() {
		Set<PaymentRecord> transactions = repository.getAllTransactions();
		return ledgerViewProjector.projectViews(transactions, ViewFactory::convertToManagerView);
	}
}
