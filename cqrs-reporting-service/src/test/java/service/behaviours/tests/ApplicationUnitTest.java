package service.behaviours.tests;

import dtupay.services.reporting.adapters.persistence.LedgerWriteRepository;
import dtupay.services.reporting.application.services.ReportingManager;
import dtupay.services.reporting.domain.entities.LedgerAggregate;
import dtupay.services.reporting.domain.entities.ReportingRole;
import dtupay.services.reporting.models.PaymentRecord;
import dtupay.services.reporting.models.Token;
import dtupay.services.reporting.query.projection.LedgerViewProjector;
import dtupay.services.reporting.query.projection.ReportProjection;
import dtupay.services.reporting.query.repositories.LedgerReadRepository;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueAsync;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueSync;
import dtupay.services.reporting.utilities.intramessaging.implementations.RabbitMqQueue;
import messaging.Event;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ApplicationUnitTest {

	private ReportingManager service;
	LedgerWriteRepository repository;
	private LedgerAggregate aggregate;
	private ReportProjection projection;

	public void setUp_async_queues() {
		MessageQueue eventQueue = new MessageQueueAsync();
		repository = new LedgerWriteRepository(eventQueue);
		LedgerReadRepository readRepository = new LedgerReadRepository();
		new LedgerViewProjector(repository, readRepository, eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue("localhost");
		service = new ReportingManager(dtupayMq, readRepository, repository);
		aggregate = service.getAggregate();
		projection = service.getProjection();
	}

	private void setup_sync_queues() {
		MessageQueue eventQueue = new MessageQueueSync();
		repository = new LedgerWriteRepository(eventQueue);
		LedgerReadRepository readRepository = new LedgerReadRepository();
		new LedgerViewProjector(repository, readRepository, eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue();
		service = new ReportingManager(dtupayMq, readRepository, repository);
		aggregate = service.getAggregate();
		projection = service.getProjection();
	}

	private void setup_rabbitmq() {
		MessageQueue eventQueue = new RabbitMqQueue("event");
		repository = new LedgerWriteRepository(eventQueue);
		LedgerReadRepository readRepository = new LedgerReadRepository();
		new LedgerViewProjector(repository, readRepository, eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue();
		service = new ReportingManager(dtupayMq, readRepository, repository);
		aggregate = service.getAggregate();
		projection = service.getProjection();
	}

	public void create_a_new_report() {
		PaymentRecord record = new PaymentRecord("custom-bank", "merchant-bank",
										1000, "", Token.random(), "customerId", "merchantId");
		var ledgerId = aggregate.createLedger(record.customerId(), ReportingRole.CUSTOMER).getId();
		var ledger = repository.getById(ledgerId);
		assertEquals("customerId", ledger.getId());
		assertEquals(ledgerId, ledger.getId());
		assertTrue(ledger.getTransactions().isEmpty());
	}
	
	public void create_ten_new_reports_concurrently() throws Exception {
		var rids = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			final int k=i;
			new Thread(() -> {try {
				if (k % 2 == 0) {
					rids.add(aggregate.createLedger("customerId"+(k+1), ReportingRole.CUSTOMER).getId());
				} else {
					rids.add(aggregate.createLedger("merchantId"+(k+2), ReportingRole.MERCHANT).getId());
				}
			} catch (Exception e) {
				throw new Error(e);
			}}).start();
		}
		Thread.sleep(3000);
		assertEquals(10, rids.size());
	}

	public void create_a_new_report_with_one_update() throws InterruptedException {
		var token = Token.random();
		var paymentRecord = new PaymentRecord("custom-bank", "merchant-bank",
												1000, "", token, "customerId", "merchantId");
		var ledgerId = aggregate.createLedger(paymentRecord.merchantId(), ReportingRole.MERCHANT).getId();

		var writtenLedger = aggregate.updateLedger(ledgerId, Set.of(paymentRecord));

		Thread.sleep(1000); // Give the repository time to update its data (-> eventual consistency)
		var ledger = repository.getById(ledgerId);
		assertEquals(writtenLedger.getId(), ledger.getId());
		assertEquals(writtenLedger.getRole(), ledger.getRole());
		var transactions = ledger.getTransactions();
		var views = projection.getMerchantViews(ledgerId);
		assertEquals(1, views.size());
		assertEquals(1, transactions.size());

		assertEquals(token, views.stream().iterator().next().token());
		assertEquals(1000, views.stream().iterator().next().amount());

		var managerReport = repository.getById("ADMIN");
		assertEquals(ReportingRole.MANAGER, managerReport.getRole());
		assertEquals(0, managerReport.getTransactions().size());
	}

	public void create_a_new_bank_transfer_log() throws InterruptedException {
		var token = Token.random();
		var payRecord = new PaymentRecord("custom-bank", "merchant-bank",
					1000, "", token, "customerId", "merchantId");
		Event event = new Event("BTC", payRecord);
		service.handleBankTransferConfirmed(event);

		Thread.sleep(1000);

		var managerLedger = repository.getById("ADMIN");
		assertEquals(1, managerLedger.getTransactions().size());

		var customerViews = projection.getCustomerViews("customerId");
		assertEquals(1, customerViews.size());

		assertEquals(2, projection.getMerchantViews("merchantId").size());
	}
	
	@Test
	public void should_run_using_rabbitmq( ) throws Exception {
		setup_rabbitmq();
		create_a_new_report();
		create_a_new_report_with_one_update();
		create_a_new_bank_transfer_log();
		create_ten_new_reports_concurrently();
//		queries_return_correct_results();
	}

	@Test
	public void should_run_using_sync_queue( ) throws Exception {
		setup_sync_queues();
		create_a_new_report();
		create_a_new_report_with_one_update();
		create_a_new_bank_transfer_log();
		create_ten_new_reports_concurrently();
//		queries_return_correct_results();

	}
	
	@Test
	public void should_run_using_async_queue( ) throws Exception {
		setUp_async_queues();
		create_a_new_report();
		create_a_new_report_with_one_update();
		create_a_new_bank_transfer_log();
		create_ten_new_reports_concurrently();
//		queries_return_correct_results();
	}

}
