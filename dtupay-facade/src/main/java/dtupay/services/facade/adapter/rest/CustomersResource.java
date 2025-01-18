package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.CustomerServiceFactory;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.CustomerService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionException;

@Path("/customers")
public class CustomersResource {

  private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
  private CustomerService customerService = new CustomerServiceFactory().getService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response register(Customer customer) throws URISyntaxException {
    logger.info("Customer registration resource accessed: {}", customer);
    try {
      Customer registeredCustomer = customerService.register(customer);
      String id = registeredCustomer.payId();
      return Response
            .created(new URI("http://localhost:8080/customers/"+id))
            .entity(registeredCustomer)
            .build();
    } catch (CompletionException exception) {
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(exception.getCause().getMessage())
              .build();
    }
  }
}
