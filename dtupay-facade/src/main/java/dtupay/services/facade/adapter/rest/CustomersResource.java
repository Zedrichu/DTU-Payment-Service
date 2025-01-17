package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.exception.AccountCreationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;

@Path("/customers")
public class CustomersResource {

  private CustomerService customerService = new CustomerFactory().getService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response register(Customer customer) throws URISyntaxException {
    try {
      Customer registeredCustomer = customerService.register(customer);
      String id = registeredCustomer.id();
      return Response
            .created(new URI("localhost:8080/customers/"+id))
            .entity(registeredCustomer)
            .build();
    } catch (AccountCreationException exception) {
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(exception.getMessage())
              .build();
    }
  }
}
