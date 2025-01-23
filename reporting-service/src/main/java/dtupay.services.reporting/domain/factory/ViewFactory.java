package dtupay.services.reporting.domain.factory;

import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.models.views.CustomerView;
import dtupay.services.reporting.domain.models.views.ManagerView;
import dtupay.services.reporting.domain.models.views.MerchantView;

public class ViewFactory {
	public CustomerView createCustomerView(PaymentRecord paymentRecord) {
		return new CustomerView(paymentRecord.merchantId(), paymentRecord.token(), paymentRecord.amount());
	}

	public MerchantView createMerchantView(PaymentRecord paymentRecord) {
		return new MerchantView(paymentRecord.token(), paymentRecord.amount());
	}

	public ManagerView createManagerView(PaymentRecord paymentRecord) {
		return new ManagerView(paymentRecord.merchantId(), paymentRecord.description(),
													 paymentRecord.token(), paymentRecord.customerId(), paymentRecord.amount());
	}
}
