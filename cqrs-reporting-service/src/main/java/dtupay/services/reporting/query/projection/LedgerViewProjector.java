package dtupay.services.reporting.query.projection;

import dtupay.services.reporting.adapters.persistence.LedgerWriteRepository;
import dtupay.services.reporting.domain.entities.ReportingRole;
import dtupay.services.reporting.domain.events.LedgerCreated;
import dtupay.services.reporting.domain.events.LedgerDeleted;
import dtupay.services.reporting.domain.events.TransactionAdded;
import dtupay.services.reporting.models.PaymentRecord;
import dtupay.services.reporting.query.repositories.LedgerReadRepository;
import dtupay.services.reporting.query.views.CustomerView;
import dtupay.services.reporting.query.views.ManagerView;
import dtupay.services.reporting.query.views.MerchantView;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;
import org.picocontainer.annotations.Inject;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/* projector: event store -> write model event -> conversion -> read model update */
public class LedgerViewProjector {

	@Inject
	private final LedgerWriteRepository writeRepository;

	@Inject
	private final LedgerReadRepository repository;

  public LedgerViewProjector(LedgerWriteRepository writeRepository, LedgerReadRepository repository, MessageQueue eventQueue) {
		this.repository = repository;
		this.writeRepository = writeRepository;

    eventQueue.addHandler(LedgerCreated.class,    e -> apply((LedgerCreated) e));
		eventQueue.addHandler(TransactionAdded.class, e -> apply((TransactionAdded) e));
		eventQueue.addHandler(LedgerDeleted.class,    e -> apply((LedgerDeleted) e));
	}

	public <T> Set<T> projectViews(Set<PaymentRecord> transactions, Function<PaymentRecord, T> projector) {
		return transactions.stream().map(projector).collect(Collectors.toSet());
	}

	public void apply(LedgerCreated event) {
		repository.initRoleLedger(event.getId(), event.getRole());
	}

	public void apply(TransactionAdded event) {
		ReportingRole role = writeRepository.getById(event.getId()).getRole();
		switch (role) {
			case CUSTOMER -> {
				Set<CustomerView> views = projectViews(Set.of(event.getTransaction()),
																							 ViewFactory::convertToCustomerView);
				repository.addCustomerViews(event.getId(), views);
			}
			case MERCHANT -> {
				Set<MerchantView> views = projectViews(Set.of(event.getTransaction()),
																							 ViewFactory::convertToMerchantView);
				repository.addMerchantViews(event.getId(), views);
			}
			default -> {
				Set<ManagerView> views = projectViews(Set.of(event.getTransaction()),
																							ViewFactory::convertToManagerView);
				repository.addManagerViews(views);
			}
		}
	}

	public void apply(LedgerDeleted event) {
		repository.removeRoleLedger(event.getId());
	}

}
