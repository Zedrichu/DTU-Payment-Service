package dtupay.services;

import dtupay.exceptions.AccountCreationException;
import dtupay.exceptions.DeregisterException;
import dtupay.exceptions.TokenRequestException;
import dtupay.model.Customer;
import dtupay.model.Token;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
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

    public ArrayList<Token> requestTokens(String customerId, int noTokens) throws TokenRequestException {

      Response response = baseURL
              .path("/customers")
              .path(customerId)
              .path("/tokens")
              .request()
              .post(Entity.entity(noTokens,MediaType.APPLICATION_JSON));

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        throw new TokenRequestException(response.readEntity(String.class));
      }

      return response.readEntity(new GenericType<ArrayList<Token>>() {});
    }

  public boolean deregister(String customerId) throws DeregisterException {
    Response response = baseURL
          .path("/customers")
          .path(customerId)
          .request()
          .delete();

    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
      throw new DeregisterException(response.readEntity(String.class));
    }
    return true;
  }
}

