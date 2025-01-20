package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.CustomerServiceFactory;
import dtupay.services.facade.domain.CustomerService;
import dtupay.services.facade.domain.models.Token;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

@Path("/customers/{cid}/tokens")
public class CustomerTokenResource {

    private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
    private CustomerService customerService = new CustomerServiceFactory().getService();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestTokens(
            @PathParam("cid") String customerId,
            @QueryParam("amount") int amount) {
        logger.info("Received request of {} tokens for customer with id {}", amount, customerId);
        ArrayList<Token> tokenList = customerService.requestTokens(amount, customerId);
        return Response.ok().entity(tokenList).build();
    }
}
