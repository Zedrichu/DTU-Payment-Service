package dtupay.services;

import dtupay.model.PaymentRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class ReportService {

	Client client = ClientBuilder.newClient();
	WebTarget baseURL = client.target("http://localhost:8080");

	public String getCustomerReport(String s) {
		return "";
	}

//	public PaymentRequest request()
//		Response response = baseURL
//					.path("/reports")
//					.path("/customers")
}
