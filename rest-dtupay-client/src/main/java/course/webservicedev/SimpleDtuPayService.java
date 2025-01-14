package course.webservicedev;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

public class SimpleDtuPayService {

   Client client = ClientBuilder.newClient();
   WebTarget baseURL = client.target("http://localhost:8080");

   public String register(Merchant merchant) {
      Response response = baseURL
            .path("/merchants")
            .request()
            .post(Entity.entity(merchant, MediaType.APPLICATION_JSON));
      return response.readEntity(String.class);
   }

   public String register(Customer customer) {
      Response response = baseURL
            .path("/customers")
            .request()
            .post(Entity.entity(customer, MediaType.APPLICATION_JSON));
      return response.readEntity(String.class);
   }

   public void deregisterCustomer(String customerId) {
      Response response = baseURL.path("/customers").path(customerId).request().delete();
      if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
         throw new UnknownAccountException(response.readEntity(String.class));
      }
   }

   public void deregisterMerchant(String merchantId) {
      Response response = baseURL.path("/merchants").path(merchantId).request().delete();
      if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
         throw new UnknownAccountException(response.readEntity(String.class));
      }
   }


   public boolean pay(Integer amount, String customerId, String merchantId) throws UnknownAccountException {
      Payment payment = new Payment(customerId, merchantId, amount);
      Response response = baseURL
            .path("/payments")
            .request()
            .post(Entity.entity(payment, MediaType.APPLICATION_JSON));
      if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
         throw new UnknownAccountException(response.readEntity(String.class));
      }
      return response.getStatus() == 201;
   }

   public ArrayList<Payment> paymentList() {
      return baseURL
            .path("/payments")
            .request()
            .get(new GenericType<ArrayList<Payment>> () {});
   }
}
