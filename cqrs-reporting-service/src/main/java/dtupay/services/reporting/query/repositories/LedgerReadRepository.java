package dtupay.services.reporting.query.repositories;

import dtupay.services.reporting.domain.events.LedgerCreated;
import dtupay.services.reporting.domain.events.LedgerDeleted;
import dtupay.services.reporting.domain.events.TransactionAdded;
import dtupay.services.reporting.models.PaymentRecord;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;

import java.util.*;
import java.util.stream.Collectors;

public class LedgerReadRepository {

    // Mapping from LedgerId -> Set of Transaction Logs
    private final Map<String, Set<PaymentRecord>> paymentRecords = new HashMap<>();

    public LedgerReadRepository(MessageQueue eventQueue) {
        eventQueue.addHandler(LedgerCreated.class, e -> apply((LedgerCreated) e));
        eventQueue.addHandler(TransactionAdded.class, e -> apply((TransactionAdded) e));
        eventQueue.addHandler(LedgerDeleted.class, e -> apply((LedgerDeleted) e));
    }

    public boolean contains(String ledgerId) {
        return paymentRecords.containsKey(ledgerId);
    }

    public void addTransactions(String ledgerId, Set<PaymentRecord> transactions) {
        paymentRecords.computeIfAbsent(ledgerId, k -> new HashSet<>()).addAll(transactions);
    }

    public void apply(LedgerCreated event) {
    }

    public void apply(TransactionAdded event) {
        var transactionsByLedger = paymentRecords.getOrDefault(event.getId(), new HashSet<>());
        transactionsByLedger.add(event.getTransaction());
        paymentRecords.put(event.getId(), transactionsByLedger);
    }

    public void apply(LedgerDeleted event) {
        var transactionsByLedger = paymentRecords.getOrDefault(event.getId(), new HashSet<>());
        transactionsByLedger.clear();
        paymentRecords.put(event.getId(), transactionsByLedger);
    }


    public Set<PaymentRecord> getTransactionsByLedger(String ledgerId) {
        return paymentRecords.getOrDefault(ledgerId, new HashSet<>());
    }

    public Set<PaymentRecord> getAllTransactions() {
        return paymentRecords.entrySet().stream()
              .flatMap(entry -> entry.getValue().stream())
              .collect(Collectors.toSet());
    }
}
