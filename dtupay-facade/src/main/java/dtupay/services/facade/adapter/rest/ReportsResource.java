package dtupay.services.facade.adapter.rest;

import dtupay.services.facade.adapter.mq.ReportServiceFactory;
import dtupay.services.facade.domain.ReportService;
import dtupay.services.facade.domain.models.Report;
import dtupay.services.facade.domain.models.views.CustomerView;
import dtupay.services.facade.domain.models.views.ManagerView;
import dtupay.services.facade.domain.models.views.MerchantView;
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

import dtupay.services.facade.annotations.ClassAuthor;
import dtupay.services.facade.annotations.MethodAuthor;

@ClassAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
@Path("/reports")
@Tag(name = "Reports Resource", description = "APIs for generating reports")
public class ReportsResource {
	private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
	private ReportService reportService = new ReportServiceFactory().getService();


	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	@GET
	@Path("/customers/{customerId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Get customer report",
			description = "Get a report of a customer's transaction based on id"
	)
	@APIResponses({
			@APIResponse(
					responseCode = "201",
					description = "Successfully retrieved customer report",
					content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Report.class)
					)
			)
	})
	public Response getCustomerReport(@PathParam("customerId") String customerId) {
		logger.info("customer report resource accessed: {}", customerId);
		Report<CustomerView> customerRep = reportService.getCustomerReport(customerId);
		return Response.ok().entity(customerRep.getEntries()).build();
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	@GET
	@Path("/merchants/{merchantId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Get merchant report",
			description = "Get a report of a merchant's transaction based on id"
	)
	@APIResponses({
			@APIResponse(
					responseCode = "201",
					description = "Successfully retrieved merchant report",
					content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Report.class)
					)
			)
	})
	public Response getMerchantReport(@PathParam("merchantId") String merchantId) {
		logger.info("merchant report resource accessed: {}", merchantId);
		Report<MerchantView> merchantRep = reportService.getMerchantReport(merchantId);
		return Response.ok().entity(merchantRep.getEntries()).build();
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	@GET
	@Path("/manager")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
			summary = "Get manager report",
			description = "Get a report of all transactions"
	)
	@APIResponses({
			@APIResponse(
					responseCode = "201",
					description = "Successfully retrieved manager report",
					content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Report.class)
					)
			)
	})
	public Response getManagerReport() {
		logger.info("manager report resource accessed");
		Report<ManagerView> managerRep = reportService.getManagerReport();
		return Response.ok().entity(managerRep.getEntries()).build();
	}

}
