package org.acme.adapters;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.MerchantService;
import org.acme.exceptions.UnknownAccountException;

@Path("/merchants/{mid}")
public class MerchantResource {

   MerchantService mService = MerchantService.getInstance();

   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   public Response unregisterMerchant(@PathParam("mid") String merchantId) {
      try {
         mService.unregister(merchantId);
         return Response.status(Response.Status.NO_CONTENT).build();
      } catch (UnknownAccountException e) {
         return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
      }
   }
}
