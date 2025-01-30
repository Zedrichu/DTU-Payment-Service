package dtupay.services.reporting.domain.projection;

import dtupay.services.reporting.domain.aggregate.Ledger;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.projection.views.CustomerView;
import dtupay.services.reporting.domain.projection.views.ManagerView;
import dtupay.services.reporting.domain.projection.views.MerchantView;
import dtupay.services.reporting.domain.repositories.LedgerReadRepository;
import org.picocontainer.annotations.Inject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LedgerViewProjector {

	@Inject
	private LedgerReadRepository repository;

	public LedgerViewProjector(LedgerReadRepository repository) {
		this.repository = repository;
	}

	public <T> Set<T> projectViews(Set<PaymentRecord> transactions, Function<PaymentRecord, T> projector) {
		return transactions.stream().map(projector).collect(Collectors.toSet());
	}

	public void projectLedger(Ledger ledger) {
		Set<PaymentRecord> transactions = Optional
					.ofNullable(repository.getTransactionsByLedger(ledger.getId()))
					.orElse(new HashSet<>());
		repository.addTransactions(ledger.getId(), ledger.getTransactions());

		switch (ledger.getRole()) {
			case CUSTOMER -> {
				Set<CustomerView> views = projectViews(ledger.getTransactions(), ViewFactory::convertToCustomerView);
//				repository.addViews(ledger.getId(), views);
			}
			case MERCHANT -> {
				Set<MerchantView> views = projectViews(ledger.getTransactions(), ViewFactory::convertToMerchantView);
//				repository.addViews(ledger.getId(), views);
			}
			default -> {
				Set<ManagerView> views = projectViews(ledger.getTransactions(), ViewFactory::convertToManagerView);
//				repository.addViews(ledger.getId(), views);
			}
		};


	}

}
