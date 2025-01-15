package course.webservicedev.adapter.rest;

import course.webservicedev.domain.CustomerService;
import course.webservicedev.domain.models.Customer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/customers")
public class CustomersResource {

  private CustomerService customerService = new CustomerService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(Customer customer) {
    String customerId = customerService.register(customer);
    return Response.ok(customerId).build();
  }
}
