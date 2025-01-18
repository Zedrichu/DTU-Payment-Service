package dtupay.services;

import dtupay.AccountCreationException;
import dtupay.model.Merchant;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class MerchantService {
	Client client = ClientBuilder.newClient();
	WebTarget baseURL = client.target("http://localhost:8080");

	public Merchant register(Merchant merchant) throws AccountCreationException {
		Response response = baseURL
					.path("/merchants")
					.request()
					.post(Entity.entity(merchant, MediaType.APPLICATION_JSON));

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			throw new AccountCreationException(response.readEntity(String.class));
		}
		return response.readEntity(Merchant.class);
	}
}
