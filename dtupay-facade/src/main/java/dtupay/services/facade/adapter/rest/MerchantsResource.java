package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.MerchantServiceFactory;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.Merchant;
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

@Path("/merchants")
public class MerchantsResource {

  private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
  private MerchantService merchantService = new MerchantServiceFactory().getService();

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response register(Merchant merchant) throws URISyntaxException {
    logger.info("Merchant registration resource accessed: {}", merchant);
    Merchant registeredMerchant = merchantService.register(merchant);
    String id = registeredMerchant.payId();
    return Response
          .created(new URI("http://localhost:8080/merchants/"+id))
          .entity(registeredMerchant)
          .build();
  }
}