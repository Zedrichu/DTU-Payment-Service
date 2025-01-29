package dtupay.services.reporting.domain.repositories;

import dtupay.services.reporting.domain.aggregate.Ledger;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;

public class LedgerRepository {

    private final EventStore eventStore;

    public LedgerRepository(MessageQueue bus) {
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
