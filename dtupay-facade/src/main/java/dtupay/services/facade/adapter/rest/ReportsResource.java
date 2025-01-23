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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dtupay.services.facade.annotations.ClassAuthor;
import dtupay.services.facade.annotations.MethodAuthor;

@ClassAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
@Path("/reports")
public class ReportsResource {
	private Logger logger = LoggerFactory.getLogger(CustomersResource.class);
	private ReportService reportService = new ReportServiceFactory().getService();


	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	@GET
	@Path("/customers/{customerId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerReport(@PathParam("customerId") String customerId) {
		logger.info("customer report resource accessed: {}", customerId);
		Report<CustomerView> customerRep = reportService.getCustomerReport(customerId);
		return Response.ok().entity(customerRep.getEntries()).build();
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	@GET
	@Path("/merchants/{merchantId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMerchantReport(@PathParam("merchantId") String merchantId) {
		logger.info("merchant report resource accessed: {}", merchantId);
		Report<MerchantView> merchantRep = reportService.getMerchantReport(merchantId);
		return Response.ok().entity(merchantRep.getEntries()).build();
	}

	@MethodAuthor(author = "Jonas Kjeldsen", stdno = "s204713")
	@GET
	@Path("/manager")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getManagerReport() {
		logger.info("manager report resource accessed: {}");
		Report<ManagerView> managerRep = reportService.getManagerReport();
		return Response.ok().entity(managerRep.getEntries()).build();
	}

}
