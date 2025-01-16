package dtupay.facade.adapter.rest;

import dtupay.facade.domain.models.Customer;
import dtupay.facade.domain.CustomerService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/customers")
public class CustomersResource {

  private CustomerService customerService = new CustomerFactory().getService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(Customer customer) {
    String customerId = customerService.register(customer);
    return Response.ok().entity(customerId).build();
  }
}
