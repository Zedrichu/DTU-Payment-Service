package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.CustomerServiceFactory;
import dtupay.services.facade.domain.models.Customer;
import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.exception.AccountDeletionException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionException;

@Path("/customers")
@Tag(name = "Customer Resource", description = "Customer management operations")
public class CustomersResource {

  private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
  private CustomerService customerService = new CustomerServiceFactory().getService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
          summary = "Register a new customer",
          description = "Registers a new customer and returns the created customer along with a URI for the resource."
  )
  @APIResponses({
          @APIResponse(
                  responseCode = "201",
                  description = "Customer successfully registered",
                  content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Customer.class))
          ),
          @APIResponse(
                  responseCode = "400",
                  description = "Invalid customer data or registration error",
                  content = @Content(mediaType = MediaType.TEXT_PLAIN)
          )
  })
  public Response register(Customer customer) throws URISyntaxException {
    logger.info("Customer registration resource accessed: {}", customer);
    try {
      Customer registeredCustomer = customerService.register(customer);
      String id = registeredCustomer.payId();
      return Response
            .created(new URI("http://localhost:8080/customers/" + id))
            .entity(registeredCustomer)
            .build();
    } catch (CompletionException exception) {
      var message = "Merchant registration failed: " + exception.getCause().getMessage();
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(message)
              .build();
    }
  }

  @DELETE
  @Path("/{customerId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
           summary = "Deregister a customer",
           description = "Deregisters a existing customer and returns a response indicating the result."
  )
  @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Customer successfully deregistered"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid merchant ID or deregistration error",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN)
            )
  })
  public Response deregister(@PathParam("customerId") String customerId) {
    logger.info("Customer deregistration resource accessed: {}", customerId);
    try {
      customerService.deregister(customerId);
      return Response.ok().build();
    } catch (CompletionException exception) {
      var message = "Customer deregistration failed: " + exception.getCause().getMessage();
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(message)
              .build();
    }
  }
}
