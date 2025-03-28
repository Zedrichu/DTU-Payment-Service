package dtupay.services;

import dtupay.model.views.CustomerView;
import dtupay.model.views.ManagerView;
import dtupay.model.views.MerchantView;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

public class ReportService {

	Client client = ClientBuilder.newClient();
	WebTarget baseURL = client.target("http://localhost:8080");

	// Gets CustomerReports no error handling needed
	public ArrayList<CustomerView> getCustomerReport(String customerId) {
		Response response = baseURL.path("/reports").
				path("/customers").
				path(customerId).request().get();
		return response.readEntity(new GenericType<ArrayList<CustomerView>>() {});
	}

	public ArrayList<MerchantView> getMerchantReport(String merchantId) {
		Response response = baseURL.path("/reports").
				path("/merchants").
				path(merchantId).request().get();
		return response.readEntity(new GenericType<ArrayList<MerchantView>>() {});
	}

	public ArrayList<ManagerView> getManagerReport() {
		Response response = baseURL.path("/reports").
				path("/manager").request().get();
		return response.readEntity(new GenericType<ArrayList<ManagerView>>() {});
	}
}
