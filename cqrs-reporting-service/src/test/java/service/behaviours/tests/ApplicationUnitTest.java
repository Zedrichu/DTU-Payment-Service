package service.behaviours.tests;

import dtupay.services.reporting.domain.ReportingManager;
import dtupay.services.reporting.domain.aggregate.ReportingRole;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.Token;
import dtupay.services.reporting.domain.repositories.LedgerReadRepository;
import dtupay.services.reporting.domain.repositories.LedgerWriteRepository;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueAsync;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueSync;
import dtupay.services.reporting.utilities.intramessaging.implementations.RabbitMqQueue;
import messaging.Event;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;


public class ApplicationUnitTest {

	private ReportingManager service;
	LedgerWriteRepository repository;

	public void setUp_async_queues() {
		MessageQueue eventQueue = new MessageQueueAsync();
		repository = new LedgerWriteRepository(eventQueue);
		LedgerReadRepository readRepository = new LedgerReadRepository(eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue("localhost");
		service = new ReportingManager(dtupayMq, readRepository, repository);
	}

	private void setup_sync_queues() throws InterruptedException, ExecutionException, Exception {
		MessageQueue eventQueue = new MessageQueueSync();
		repository = new LedgerWriteRepository(eventQueue);
		LedgerReadRepository readRepository = new LedgerReadRepository(eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue();
		service = new ReportingManager(dtupayMq, readRepository, repository);
	}

	private void setup_rabbitmq() throws InterruptedException, ExecutionException, Exception {
		MessageQueue eventQueue = new RabbitMqQueue("event");
		repository = new LedgerWriteRepository(eventQueue);
		LedgerReadRepository readRepository = new LedgerReadRepository(eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue();
		service = new ReportingManager(dtupayMq, readRepository, repository);
	}

	public void create_a_new_report() throws InterruptedException, ExecutionException {
		PaymentRecord record = new PaymentRecord("custom-bank", "merchant-bank",
										1000, "", Token.random(), "customerId", "merchantId");
		var ledgerId = service.createLedger(record.customerId(), ReportingRole.CUSTOMER);
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
					rids.add(service.createLedger("customerId"+(k+1), ReportingRole.CUSTOMER));
				} else {
					rids.add(service.createLedger("merchantId"+(k+2), ReportingRole.MERCHANT));
				}
			} catch (Exception e) {
				throw new Error(e);
			}}).start();
		}
		Thread.sleep(3000);
		assertEquals(10, rids.size());
	}

	public void create_a_new_report_with_one_update() throws InterruptedException, ExecutionException, IllegalAccessException {
		var token = Token.random();
		var paymentRecord = new PaymentRecord("custom-bank", "merchant-bank",
												1000, "", token, "customerId", "merchantId");
		var ledgerId = service.createLedger(paymentRecord.merchantId(), ReportingRole.MERCHANT);

		service.updateLedger(ledgerId, Set.of(paymentRecord));

		Thread.sleep(1000); // Give the repository time to update its data (-> eventual consistency)
		var report = repository.getById(ledgerId);
		assertEquals("merchantId", report.getId());
		assertEquals(ReportingRole.MERCHANT, report.getRole());
		var transactions = report.getTransactions();
		var views = service.getMerchantViews(ledgerId);
		assertEquals(1, views.size());
		assertEquals(1, transactions.size());

		assertEquals(token, views.stream().iterator().next().getToken());
		assertEquals(1000, views.stream().iterator().next().getAmount());

		var managerReport = repository.getById("ADMIN");
		assertEquals(ReportingRole.MANAGER, managerReport.getRole());
		assertEquals(0, managerReport.getTransactions().size());
	}

	public void create_a_new_bank_transfer_log() throws InterruptedException, ExecutionException, IllegalAccessException {
		var token = Token.random();
		var payRecord = new PaymentRecord("custom-bank", "merchant-bank",
					1000, "", token, "customerId", "merchantId");
		Event event = new Event("BTC", payRecord);
		service.handleBankTransferConfirmed(event);

		Thread.sleep(2000);

		var managerLedger = repository.getById("ADMIN");
		assertEquals(1, managerLedger.getTransactions().size());

		var customerViews = service.getCustomerViews("customerId");
		assertEquals(1, customerViews.size());

		assertEquals(2, service.getMerchantViews("merchantId").size());
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
