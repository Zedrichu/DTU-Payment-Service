package dtupay.services.reporting.query.projection;

import dtupay.services.reporting.models.PaymentRecord;
import dtupay.services.reporting.query.views.CustomerView;
import dtupay.services.reporting.query.views.ManagerView;
import dtupay.services.reporting.query.views.MerchantView;

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
