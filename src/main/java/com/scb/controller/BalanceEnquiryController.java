package com.scb.controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.scb.model.BalanceEnquiryRequest;
import com.scb.model.BalanceEnquiryResponse;
import com.scb.service.CustomerRequestService;
import com.scb.utils.ContextPathConstants;
import com.scb.utils.SCBCommonMethods;

import lombok.extern.log4j.Log4j2;


@RestController @Log4j2
@RequestMapping(ContextPathConstants.CUSTOMER_URL)
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BalanceEnquiryController {
	@Autowired
	private CustomerRequestService customerRequestService;
	@Autowired
	private SCBCommonMethods commonMethods;

	@RequestMapping(value = ContextPathConstants.CUSTOMER_REQUEST_HANDLE_URL, method = RequestMethod.POST, produces = { "application/json", "application/xml" })
	public BalanceEnquiryResponse customerRequestHandle(@RequestHeader Map<String, String> requestMap, @RequestBody String balanceEnquiryRequest) {
		log.info("RequestHeader received "+ requestMap);
		//log.info("Request received "+ balanceEnquiryRequest.toString());
		String contentType = requestMap.get("content-type");
		
//		if(CommonConstants.APPLICATION_XML.equalsIgnoreCase(contentType)) {
//			RequestParser parser = new XmlParser();
//			parser.parse(balanceEnquiryRequest);
//		}
		
		BalanceEnquiryResponse customerResponse = new BalanceEnquiryResponse();
		
		customerResponse = customerRequestService.requestHandleService(requestMap, balanceEnquiryRequest);
		//customerResponse = customerRequestService.customerRequestHandleService(balanceEnquiryRequest);
		/*if (commonMethods.isValidateCustomerRequest(customerRequest)) {
			customerResponse = customerRequestService.customerRequestHandleService(customerRequest);
		} else {
			log.info(" Validation failed");
			customerResponse = commonMethods.getErrorResponse("Request Validation Error");
		}*/
		log.info("Response: "+customerResponse.toString());
		return customerResponse;
	}

	@RequestMapping(value = ContextPathConstants.CUSTOMER_REQUEST_HANDLE_URL_REQUEST)
	public BalanceEnquiryRequest customerRequestHandleExampleRequest() {

		return BalanceEnquiryRequest.builder().customerAccType("Saving").customerId(22).customerName("Test Customer")
				.customerRegion("India").correlationId(200).build();
		// return
		// customerRequestService.customerRequestHandleService(customerRequest);

	}

}
