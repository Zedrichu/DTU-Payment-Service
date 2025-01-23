package dtupay.services.reporting.domain.factory;

import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.views.CustomerView;
import dtupay.services.reporting.domain.models.views.ManagerView;
import dtupay.services.reporting.domain.models.views.MerchantView;
import dtupay.services.reporting.domain.models.views.ReportView;

public class ViewFactory {
	public CustomerView createCustomerView(PaymentRecord paymentRecord) {
		return new CustomerView(paymentRecord.merchantId(), paymentRecord.token(), paymentRecord.amount());
	}

	public MerchantView createMerchantView(PaymentRecord paymentRecord) {
		return new MerchantView(paymentRecord.token(), paymentRecord.amount());
	}

	public ManagerView createManagerView(PaymentRecord paymentRecord) {
		return new ManagerView(paymentRecord.customerId(), paymentRecord.merchantId(),
													 paymentRecord.token(), paymentRecord.amount());
	}

	public ManagerView convertCustomerView(String customerId, CustomerView customerView) {
		return new ManagerView(customerId, customerView.getMerchantId(),
														customerView.getToken(), customerView.getAmount());
	}

	public ReportView createCustomerReportView(PaymentRecord paymentRecord) {
		var report = new ReportView();
		report.setMerchantId(paymentRecord.merchantId());
		report.setToken(paymentRecord.token());
		report.setAmount(paymentRecord.amount());
		return report;
	}

	public ReportView createMerchantReportView(PaymentRecord paymentRecord) {
		var report = new ReportView();
		report.setAmount(paymentRecord.amount());
		report.setToken(paymentRecord.token());
		return report;
	}

	public ReportView createManagerReportView(PaymentRecord paymentRecord) {
		var report = new ReportView();
		report.setCustomerId(paymentRecord.customerId());
		report.setMerchantId(paymentRecord.merchantId());
		report.setAmount(paymentRecord.amount());
		report.setToken(paymentRecord.token());
		return report;
	}
}
