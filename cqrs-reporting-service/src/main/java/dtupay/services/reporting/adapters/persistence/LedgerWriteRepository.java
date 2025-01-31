package dtupay.services.reporting.adapters.persistence;

import dtupay.services.reporting.domain.entities.Ledger;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;

public class LedgerWriteRepository {

    private final EventStore eventStore;

    public LedgerWriteRepository(MessageQueue bus) {
        eventStore = new EventStore(bus);
    }

    public void save(Ledger ledger) {
        eventStore.addEvents(ledger.getId(), ledger.getAppliedEvents());
        ledger.clearAppliedEvents();
    }

    public Ledger getById(String reportId) {
        return Ledger.createFromEvents(eventStore.getEventsFor(reportId));
    }
}
