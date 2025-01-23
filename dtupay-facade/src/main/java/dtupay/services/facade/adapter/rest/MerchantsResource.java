package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.MerchantServiceFactory;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.domain.models.PaymentRequest;
import dtupay.services.facade.domain.models.Token;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.exception.AccountDeletionException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionException;

@Path("/merchants")
@Tag(name = "Merchant Resource", description = "Merchant management operations")
public class MerchantsResource {

  private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
  private MerchantService merchantService = new MerchantServiceFactory().getService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
          summary = "Merchant registration",
          description = "Registers a new merchant and returns the created merchant along with unique id for the resource."
  )
  @APIResponses({
          @APIResponse(
                  responseCode = "201",
                  description = "Merchant successfully registered when returned with id",
                  content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Merchant.class))
          ),
          @APIResponse(
                  responseCode = "400",
                  description = "Invalid merchant data or registration error",
                  content = @Content(mediaType = MediaType.TEXT_PLAIN)
          )
  })
  public Response register(Merchant merchant) throws URISyntaxException {
    logger.info("Merchant registration resource accessed: {}", merchant);
    try {
      Merchant registeredMerchant = merchantService.register(merchant);
      String id = registeredMerchant.payId();
      return Response
            .created(new URI("http://localhost:8080/merchants/"+id))
            .entity(registeredMerchant)
            .build();
  } catch (CompletionException exception) {
      var message = exception.getCause().getMessage();
      return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(message)
            .build();
    }
  }

  @DELETE
  @Path("/{merchantId}")
  @Operation(
          summary = "Merchant deregistration",
          description = "Deregisters a merchant when presented an existing id and returns a response."
  )
  @APIResponses({
          @APIResponse(
                  responseCode = "200",
                  description = "Merchant successfully deregistered"
          ),
          @APIResponse(
                  responseCode = "400",
                  description = "Invalid merchant id or deregistration error",
                  content = @Content(mediaType = MediaType.TEXT_PLAIN)
          )
  })
  public Response deregister(@PathParam("merchantId") String merchantId) {
    logger.info("Merchant deregistration resource accessed: {}", merchantId);
    try {
      merchantService.deregister(merchantId);
      return Response.ok().build();
    } catch (CompletionException exception) {
      var message = exception.getCause().getMessage();
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(message)
              .build();
    }
  }
}