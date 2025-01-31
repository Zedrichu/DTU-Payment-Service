package dtupay.services.reporting.domain.entities;

import dtupay.services.reporting.adapters.persistence.LedgerWriteRepository;
import dtupay.services.reporting.models.PaymentRecord;

import java.util.Set;


/* Aggregate: command -> aggregate handler -> events -> EventStore */
public class LedgerAggregate {
	private final LedgerWriteRepository writeRepository;

	public LedgerAggregate(LedgerWriteRepository writeRepository) {
		this.writeRepository = writeRepository;
	}

	public Ledger createLedger(String ledgerId, ReportingRole role) {
		// handle CreateLedgerCommand from service

		// Create a transaction ledger for the entity
		Ledger ledger = Ledger.create(ledgerId, role);
		writeRepository.save(ledger);
		return ledger;
	}

	public Ledger updateLedger(String ledgerId, Set<PaymentRecord> paymentRecords) {
		// handle UpdateLedgerCommand from service

		Ledger ledger = writeRepository.getById(ledgerId);
		ledger.update(paymentRecords);
		writeRepository.save(ledger);
		return ledger;
	}
}
