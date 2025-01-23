package dtupay.services;

import dtupay.model.PaymentRequest;
import dtupay.model.Token;
import dtupay.model.views.CustomerView;
import dtupay.model.views.MerchantView;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
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
				path("/merchant").
				path(merchantId).request().get();
		return response.readEntity(new GenericType<ArrayList<MerchantView>>() {});
	}

//	public PaymentRequest request()
//		public Customer register(Customer customer) throws AccountCreationException {
//    Response response = baseURL
//          .path("/customers")
//          .request()
//          .post(Entity.entity(customer, MediaType.APPLICATION_JSON));
//
//    if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
//      throw new AccountCreationException(response.readEntity(String.class));
//    }
//    return response.readEntity(Customer.class);Response response = baseURL
//					.path("/reports")
//					.path("/customers")
}
