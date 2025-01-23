package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.CustomerServiceFactory;
import dtupay.services.facade.adapter.mq.MerchantServiceFactory;
import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.MerchantService;
import dtupay.services.facade.domain.models.PaymentRequest;
import dtupay.services.facade.domain.models.Token;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


import java.util.ArrayList;
import java.util.concurrent.CompletionException;

@Path("/merchants/{mid}/payments")
public class MerchantPaymentResource {

    private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
    private MerchantService merchantService = new MerchantServiceFactory().getService();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requestTokens(PaymentRequest paymentRequest) {
        logger.info("Merchant payment request received: {}", paymentRequest);
        try {
            var paymentSuccess = merchantService.pay(paymentRequest);
            return Response.ok().build();
        } catch (CompletionException e) {
            String error = "Payment failed " + e.getCause().getMessage();
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

}
