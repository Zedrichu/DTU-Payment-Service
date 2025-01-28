package service.behaviours.tests;

import dtupay.services.reporting.domain.ReportingManager;
import dtupay.services.reporting.domain.aggregate.views.MerchantView;
import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.Token;
import dtupay.services.reporting.domain.repositories.ReadModelRepository;
import dtupay.services.reporting.domain.repositories.ReportRepository;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueAsync;
import dtupay.services.reporting.utilities.intramessaging.implementations.MessageQueueSync;
import dtupay.services.reporting.utilities.intramessaging.implementations.RabbitMqQueue;
import messaging.Event;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ApplicationUnitTest {

	private ReportingManager service;
	ReportRepository repository;

	public void setUp_async_queues() {
		MessageQueue eventQueue = new MessageQueueAsync();
		repository = new ReportRepository(eventQueue);
		ReadModelRepository readRepository = new ReadModelRepository(eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue("localhost");
		service = new ReportingManager(dtupayMq, readRepository, repository);
	}

	private void setup_sync_queues() throws InterruptedException, ExecutionException, Exception {
		MessageQueue eventQueue = new MessageQueueSync();
		repository = new ReportRepository(eventQueue);
		ReadModelRepository readRepository = new ReadModelRepository(eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue();
		service = new ReportingManager(dtupayMq, readRepository, repository);
	}

	private void setup_rabbitmq() throws InterruptedException, ExecutionException, Exception {
		MessageQueue eventQueue = new RabbitMqQueue("event");
		repository = new ReportRepository(eventQueue);
		ReadModelRepository readRepository = new ReadModelRepository(eventQueue);
		messaging.MessageQueue dtupayMq = new messaging.implementations.RabbitMqQueue();
		service = new ReportingManager(dtupayMq, readRepository, repository);
	}

	public void create_a_new_report() throws InterruptedException, ExecutionException {
		var reportId = service.createCustomerReport(
					new PaymentRecord("custom-bank", "merchant-bank",
								1000, "", Token.random(), "customerId", "merchantId"));
		var report = repository.getById(reportId);
		assertEquals("customerId", report.getReportId());
		assertEquals(reportId, report.getReportId());
		assertTrue(report.getCustomerViews().isEmpty());
		assertTrue(report.getMerchantViews().isEmpty());
	}
	
	public void create_ten_new_reports_concurrently() throws Exception {
		var rids = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			final int k=i;
			new Thread(() -> {try {
				if (k % 2 == 0) {
					rids.add(service.createCustomerReport(
								new PaymentRecord("", "", 1000,
											"", Token.random(), "customerId"+(k+1), "merchantId"+k)));
				} else {
					rids.add(service.createMerchantReport(
								new PaymentRecord("", "", 1000,
											"", Token.random(), "customerId"+k, "merchantId"+(k+2))));
				}
			} catch (Exception e) {
				throw new Error(e);
			}}).start();
		}
		Thread.sleep(3000);
		assertEquals(10, rids.size());
	}

	public void create_a_new_report_with_one_update() throws InterruptedException, ExecutionException {
		var token = Token.random();
		var reportId = service.createMerchantReport(
					new PaymentRecord("custom-bank", "merchant-bank",
								1000, "", token, "customerId", "merchantId"));
		service.updateReport(reportId, Set.of(), Set.of(new MerchantView(1000, token)));

		Thread.sleep(100); // Give the repository time to update its data (-> eventual consistency)
		var report = repository.getById(reportId);
		assertEquals("merchantId", report.getReportId());
		assertEquals("merchant", report.getRole());
		var views = report.getMerchantViews();
		assertEquals(1, views.size());
		assertEquals(0, report.getCustomerViews().size());
		assertEquals(token, views.stream().iterator().next().getToken());
		assertEquals(1000, views.stream().iterator().next().getAmount());

		var managerReport = repository.getById("admin");
		assertEquals("manager", managerReport.getRole());
		assertEquals(0, managerReport.getMerchantViews().size());
	}

	public void create_a_new_bank_transfer_log() throws InterruptedException, ExecutionException {
		var token = Token.random();
		var payRecord = new PaymentRecord("custom-bank", "merchant-bank",
					1000, "", token, "customerId", "merchantId");
		Event event = new Event("BTC", payRecord);
		service.handleBankTransferConfirmed(event);

		Thread.sleep(2000);

		var report = repository.getById("admin");

	}
//
//	public void queries_return_correct_results() throws Exception {
//		var userId = service.createUser("Kumar", "Chandrakant");
//		service.updateUser(userId,
//				Stream.of(new Address("New York", "NY", "10001"), new Address("Los Angeles", "CA", "90001"))
//						.collect(Collectors.toSet()),
//				Stream.of(new Contact("EMAIL", "tom.sawyer@gmail.com"), new Contact("EMAIL", "tom.sawyer@rediff.com"))
//						.collect(Collectors.toSet()));
//
//		service.updateUser(userId,
//				Stream.of(new Address("New York", "NY", "10001"), new Address("Housten", "TX", "77001"))
//						.collect(Collectors.toSet()),
//				Stream.of(new Contact("EMAIL", "tom.sawyer@gmail.com"), new Contact("PHONE", "700-000-0001"))
//						.collect(Collectors.toSet()));
//		Thread.sleep(1000);
//		assertEquals(Stream.of(new Contact("EMAIL", "tom.sawyer@gmail.com")).collect(Collectors.toSet()),
//				service.contactByType(userId, "EMAIL"));
//
//		assertEquals(Stream.of(new Address("New York", "NY", "10001")).collect(Collectors.toSet()),
//				service.addressByRegion(userId, "NY"));
//
//	}
	
	@Test
	public void should_run_using_rabbitmq( ) throws Exception {
		setup_rabbitmq();
		create_a_new_report();
		create_a_new_report_with_one_update();
//		create_ten_new_users_concurrently();
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
		create_ten_new_reports_concurrently();
//		queries_return_correct_results();
	}

}
