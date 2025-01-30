package dtupay.services.reporting.domain.aggregate;

import dtupay.services.reporting.domain.events.Event;
import dtupay.services.reporting.domain.events.LedgerCreated;
import dtupay.services.reporting.domain.events.LedgerDeleted;
import dtupay.services.reporting.domain.events.TransactionAdded;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.utilities.intramessaging.Message;

import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

// <<Aggregate Root>> - keeps track of constraints
// <<Entity>> Mutable objects, with object identity
@Getter
public class Ledger {
    private String id;          // <<Value>> object
    private ReportingRole role; // <<Value>> object

    private final Set<PaymentRecord> transactions = new HashSet<>();

    private final List<Event> appliedEvents = new ArrayList<Event>();

    private final Map<Class<? extends Message>, Consumer<Message>> handlers = new HashMap<>();

    public static Ledger create(String id, ReportingRole role) {
        LedgerCreated event = new LedgerCreated(id, role);

        var ledger = new Ledger();
        ledger.id = id;
        ledger.role = role;

        ledger.appliedEvents.add(event);
        return ledger;
    }

    // cid -> arrayList ==> cid --> Set<CustomerViews>
    public static Ledger createFromEvents(Stream<Event> events) {
        Ledger report = new Ledger();
        report.applyEvents(events);
        return report;
    }

    public Ledger() {
        registerEventHandlers();
    }

    private void registerEventHandlers() {
        handlers.put(TransactionAdded.class, e -> apply((TransactionAdded) e));
        handlers.put(LedgerDeleted.class, e -> apply((LedgerDeleted) e));
        handlers.put(LedgerCreated.class, e -> apply((LedgerCreated) e));
    }

    public void update(Set<PaymentRecord> transactions) {
        addNewTransactions(transactions);
        applyEvents(appliedEvents.stream());
    }

    private void addNewTransactions(Set<PaymentRecord> transactions){
        var events = transactions.stream().filter(a -> !getTransactions().contains(a))
              .map(record -> (Event) new TransactionAdded(this.id, record))
              .toList();
        appliedEvents.addAll(events);
    }

    /* Event Handling */
    public void applyEvents(Stream<Event> events) {
        events.forEachOrdered(this::applyEvent);
        if (this.getId() == null) {
            throw new Error("Ledger ID does not exist");
        }
    }

    private void applyEvent(Event e) {
        handlers.getOrDefault(e.getClass(), this::missingHandler).accept(e);
    }

    private void missingHandler(Message e) {
        throw new Error("handler for event "+e+" missing");
    }

    private void apply(LedgerCreated event) {
        this.id = event.getId();
        this.role = event.getRole();
    }

    private void apply(TransactionAdded event) {
        var transaction = event.getTransaction();
        this.transactions.add(transaction);
    }

    private void apply(LedgerDeleted event) {
        this.transactions.clear();
    }

    public void clearAppliedEvents() { appliedEvents.clear();
    }
}
