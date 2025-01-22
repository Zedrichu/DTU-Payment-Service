package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.MerchantServiceFactory;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.Merchant;
import dtupay.services.facade.domain.models.PaymentRequest;
import dtupay.services.facade.exception.AccountCreationException;
import dtupay.services.facade.exception.AccountDeletionException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionException;

@Path("/merchants")
public class MerchantsResource {

  private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
  private MerchantService merchantService = new MerchantServiceFactory().getService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
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
      return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(exception.getCause().getMessage())
            .build();
    }
  }

  @DELETE
  @Path("/{merchantId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deregister(@PathParam("merchantId") String merchantId) {
    logger.info("Merchant deregistration resource accessed: {}", merchantId);
    try {
      String response = merchantService.deregister(merchantId);
      return Response.ok().entity(response).build();
    } catch (AccountDeletionException exception) {
      return Response
              .status(Response.Status.BAD_REQUEST)
              .entity(exception.getCause().getMessage())
              .build();
    }
  }
}