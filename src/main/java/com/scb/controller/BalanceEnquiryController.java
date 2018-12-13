package com.scb.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.scb.model.AuditLog;
import com.scb.model.BalanceEnquiryRequest;
import com.scb.model.BalanceEnquiryResponse;
import com.scb.model.OutwardBalanceEnquiry;
import com.scb.service.BalanceEnquiryService;
import com.scb.serviceImpl.GcgInternalApiCall;
import com.scb.utils.CommonConstants;
import com.scb.utils.ContextPathConstants;
import com.scb.utils.RequestParser;
import com.scb.utils.SCBCommonMethods;
import com.scb.utils.XmlParser;

import lombok.extern.log4j.Log4j2;

@RestController 
@Log4j2
@RequestMapping(ContextPathConstants.CUSTOMER_URL)
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BalanceEnquiryController {
	@Autowired
	private BalanceEnquiryService balanceEnquiryService;
	
	@Autowired
	private SCBCommonMethods commonMethods;
	
	@Autowired
	private GcgInternalApiCall gcgInternalApiCall;

	@RequestMapping(value = ContextPathConstants.CUSTOMER_REQUEST_HANDLE_URL, method = RequestMethod.POST, produces = { "application/json", "application/xml" })
	public BalanceEnquiryResponse customerRequestHandle(@RequestHeader Map<String, String> requestMap, @RequestBody String balanceEnquiryRequest) {
		log.info("RequestHeader received "+ requestMap);
		//log.info("Request received "+ balanceEnquiryRequest.toString());
		String contentType = requestMap.get("content-type");
		
		OutwardBalanceEnquiry balanceEnquiry = null;
		if(CommonConstants.APPLICATION_XML.equalsIgnoreCase(contentType)) {
			RequestParser parser = new XmlParser();
			balanceEnquiry = (OutwardBalanceEnquiry) parser.parse(balanceEnquiryRequest);
		}
		
		AuditLog auditLog = commonMethods.getAuditLog(balanceEnquiry, "RECEIVED", "Request processing initiated");
		ResponseEntity<AuditLog> responseAuditLog = gcgInternalApiCall.auditLogApiCall(auditLog);
		
		BalanceEnquiryResponse enquiryResponse = new BalanceEnquiryResponse();
		
		enquiryResponse = balanceEnquiryService.requestHandleService(requestMap, balanceEnquiryRequest);

		if (enquiryResponse.getResponseCode() != 200) {
			auditLog = commonMethods.getAuditLog(balanceEnquiry, "FAILED", "Request processing failed");
		} else {
			auditLog = commonMethods.getAuditLog(balanceEnquiry, "COMPLETED", "Request processed successfully");
		}
		
		responseAuditLog = gcgInternalApiCall.auditLogApiCall(auditLog);
		log.info("Balance Enquiry Response: " + enquiryResponse.toString());
		return  enquiryResponse;
	}

	@RequestMapping(value = ContextPathConstants.CUSTOMER_REQUEST_HANDLE_URL_REQUEST)
	public BalanceEnquiryRequest customerRequestHandleExampleRequest() {

		return BalanceEnquiryRequest.builder().customerAccType("Saving").customerId(22).customerName("Test Customer")
				.customerRegion("India").correlationId(200).build();
	}
}
