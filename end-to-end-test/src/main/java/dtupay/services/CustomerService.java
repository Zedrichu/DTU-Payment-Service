package dtupay.services;

import dtupay.AccountCreationException;
import dtupay.model.Customer;
import dtupay.model.Token;
import dtupay.model.TokenRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

public class CustomerService {

  Client client = ClientBuilder.newClient();
  WebTarget baseURL = client.target("http://localhost:8080");

  public Customer register(Customer customer) throws AccountCreationException {
    Response response = baseURL
          .path("/customers")
          .request()
          .post(Entity.entity(customer, MediaType.APPLICATION_JSON));

    if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
      throw new AccountCreationException(response.readEntity(String.class));
    }
    return response.readEntity(Customer.class);
  }

    public ArrayList<Token> requestTokens(TokenRequest tokenRequest) {
      /*
      Response response = baseURL
              .path("/tokens")
              .request()
              .get(Entity.entity(tokenRequest, MediaType.APPLICATION_JSON));

      return response.readEntity(ArrayList.class);
       */
      return null;
    }
}

