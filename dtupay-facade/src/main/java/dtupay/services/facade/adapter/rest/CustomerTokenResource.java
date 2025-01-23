package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.CustomerServiceFactory;
import dtupay.services.facade.annotations.MethodAuthor;
import dtupay.services.facade.domain.CustomerService;
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

@Path("/customers/{cid}/tokens")
@Tag(name = "Customer Tokens", description = "APIs for customers requesting tokens")
public class CustomerTokenResource {

    private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
    private CustomerService customerService = new CustomerServiceFactory().getService();


    @MethodAuthor(author = "Jeppe", stdno = "s204708")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Request tokens for a customer",
            description = "Allows a customer to request a specific number of tokens."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Successfully retrieved tokens",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Token.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid input or customer ID",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN)
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN)
            )
    })
    public Response requestTokens(
            @Parameter(
                    description = "Customer ID for which the tokens are requested",
                    required = true,
                    example = "12345"
            )
            @PathParam("cid") String customerId,
            @Parameter(
                    description = "Number of tokens to request",
                    required = true,
                    example = "5"
            )
            int amount) {
        logger.info("Received request of {} tokens for customer with id {}", amount, customerId);
        try {
          ArrayList<Token> tokenList = customerService.requestTokens(amount, customerId);
          return Response.ok().entity(tokenList).build();
        } catch (CompletionException exception) {
            var message = "Token request failed: " + exception.getCause().getMessage();
          return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(message)
                .build();
        }
    }
}
