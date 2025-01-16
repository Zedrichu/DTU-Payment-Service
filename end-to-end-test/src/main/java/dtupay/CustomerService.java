package dtupay;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CustomerService {
  Client client = ClientBuilder.newClient();
  WebTarget baseURL = client.target("http://localhost:8080");
  public String register(Customer customer) {
    Response response = baseURL
          .path("/customers")
          .request()
          .post(Entity.entity(customer, MediaType.APPLICATION_JSON));
//    Response.status(Response.Status.NO_CONTENT);
    return response.readEntity(String.class);
  }
}
