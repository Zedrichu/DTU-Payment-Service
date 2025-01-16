package dtupay.facade.adapter.rest;

import dtupay.facade.domain.MerchantService;
import dtupay.facade.domain.models.Merchant;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

//@Path("/merchant")
//public class MerchantResource {
//
//  private MerchantService merchantService = new MerchantFactory().getService();
//
//  @POST
//  @Consumes(MediaType.APPLICATION_JSON)
//  public Response register(Merchant merchant) {
//    String merchantId = merchantService.register(merchant);
//    return Response.ok(merchantId).build();
//  }
//}