package dtupay.services.reporting.domain.projection;

import dtupay.services.reporting.domain.models.PaymentRecord;
import dtupay.services.reporting.domain.projection.views.CustomerView;
import dtupay.services.reporting.domain.projection.views.ManagerView;
import dtupay.services.reporting.domain.projection.views.MerchantView;

public class ViewFactory {

	public static CustomerView convertToCustomerView(PaymentRecord record) {
		return new CustomerView(record.amount(), record.merchantId(), record.token());
	}

	public static MerchantView convertToMerchantView(PaymentRecord record) {
		return new MerchantView(record.amount(), record.token());
	}

	public static ManagerView convertToManagerView(PaymentRecord record) {
		return new ManagerView(record.customerId(), record.merchantId(), record.token(), record.amount());
	}

}
