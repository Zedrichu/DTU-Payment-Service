package dtupay.services.facade.domain;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dtupay.services.facade.domain.models.views.CustomerView;
import dtupay.services.facade.domain.models.views.ManagerView;
import dtupay.services.facade.domain.models.views.MerchantView;
import dtupay.services.facade.domain.models.Report;
import dtupay.services.facade.utilities.Correlator;
import dtupay.services.facade.utilities.EventTypes;
import messaging.MessageQueue;
import messaging.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import dtupay.services.facade.annotations.ClassAuthor;
import dtupay.services.facade.annotations.MethodAuthor;

@ClassAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
public class ReportService {

	private Logger logger = LoggerFactory.getLogger(MerchantService.class);
	private MessageQueue mque;
	private Map<Correlator, CompletableFuture<Report<CustomerView>>> customerReportCorrelations = new ConcurrentHashMap<>();
	private Map<Correlator, CompletableFuture<Report<MerchantView>>> merchantReportCorrelations = new ConcurrentHashMap<>();
	private Map<Correlator, CompletableFuture<Report<ManagerView>>> managerReportCorrelations = new ConcurrentHashMap<>();

	public ReportService(MessageQueue messageQueue) {
		logger.info("facade.MerchantService instantiated");
		this.mque = messageQueue;

		this.mque.addHandler(EventTypes.CUSTOMER_REPORT_GENERATED.getTopic(), this::handleCustomerReportGenerated);
		this.mque.addHandler(EventTypes.MERCHANT_REPORT_GENERATED.getTopic(), this::handleMerchantReportGenerated);
		this.mque.addHandler(EventTypes.MANAGER_REPORT_GENERATED.getTopic(), this::handleManagerReportGenerated);
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	public Report<CustomerView> getCustomerReport(String id) {
		logger.debug("Customer report request for: {}", id);
		var correlationId = Correlator.random();
		customerReportCorrelations.put(correlationId, new CompletableFuture<>());
		Event event = new Event(EventTypes.CUSTOMER_REPORT_REQUESTED.getTopic(), new Object[] { id, correlationId });
		mque.publish(event);
		return customerReportCorrelations.get(correlationId).join();
	}

	public Report<MerchantView> getMerchantReport(String id) {
		logger.debug("Merchant report request for: {}", id);
		var correlationId = Correlator.random();
		merchantReportCorrelations.put(correlationId, new CompletableFuture<>());
		Event event = new Event(EventTypes.MERCHANT_REPORT_GENERATED.getTopic(), new Object[] { id, correlationId });
		mque.publish(event);
		return merchantReportCorrelations.get(correlationId).join();
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	public Report<ManagerView> getManagerReport() {
		logger.debug("Manager report request for: {}");
		var correlationId = Correlator.random();
		merchantReportCorrelations.put(correlationId, new CompletableFuture<>());
		Event event = new Event(EventTypes.MANAGER_REPORT_REQUESTED.getTopic(), new Object[] { correlationId });
		mque.publish(event);
		return managerReportCorrelations.get(correlationId).join();
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	public void handleCustomerReportGenerated(Event event) {
		Report<CustomerView> customerReport = event.getArgument(0,
				new Report<CustomerView>(new ArrayList<>()) {}.getClass().getGenericSuperclass());
		var core = event.getArgument(1, Correlator.class);
		customerReportCorrelations.get(core).complete(customerReport);
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	public void handleMerchantReportGenerated(Event event) {
		Report<MerchantView> merchantReport = event.getArgument(0,
				new Report<MerchantView>(new ArrayList<>()) {}.getClass().getGenericSuperclass());
		var core = event.getArgument(1, Correlator.class);
		merchantReportCorrelations.get(core).complete(merchantReport);
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	public void handleManagerReportGenerated(Event event) {
		Report<ManagerView> managerReport = event.getArgument(0,
				new Report<ManagerView>(new ArrayList<>()) {}.getClass().getGenericSuperclass());
		var core = event.getArgument(1, Correlator.class);
		managerReportCorrelations.get(core).complete(managerReport);
	}

}
