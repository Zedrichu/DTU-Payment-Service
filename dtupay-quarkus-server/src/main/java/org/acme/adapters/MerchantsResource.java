package org.acme.adapters;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.model.Merchant;
import org.acme.domain.MerchantService;

@Path("/merchants")
public class MerchantsResource {

   MerchantService mService = MerchantService.getInstance();

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response registerMerchant(Merchant merchant) {
      String customerId = mService.register(merchant);
      return Response.ok().entity(customerId).build();
   }
}
