package org.acme.adapters;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.model.Payment;
import org.acme.domain.PaymentService;

import java.util.ArrayList;

@Path("/payments")
public class PaymentsResource {

   PaymentService service = new PaymentService();

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response requestPayments() {
      ArrayList<Payment> payments = service.getPayments();
      return Response.ok().entity(payments).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response addPayment(Payment payment) {
      try {
         service.addPayment(payment);
         return Response.status(Response.Status.CREATED).build();
      } catch (Throwable e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }


}
