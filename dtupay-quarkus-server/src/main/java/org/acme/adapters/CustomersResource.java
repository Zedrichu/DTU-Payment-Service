package org.acme.adapters;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.model.Customer;
import org.acme.domain.CustomerService;

@Path("/customers")
public class CustomersResource {

   CustomerService cservice = CustomerService.getInstance();

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response registerCustomer(Customer customer) {
      String customerId = cservice.register(customer);
      return Response.ok().entity(customerId).build();
   }
}
