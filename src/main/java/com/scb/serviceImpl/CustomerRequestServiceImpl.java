package com.scb.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.scb.config.BalanceEnquiryConfig;
import com.scb.model.BalanceEnquiry;
import com.scb.model.BalanceEnquiryRequest;
import com.scb.model.BalanceEnquiryResponse;
import com.scb.model.MsAuditLog;
import com.scb.model.OutwardBalanceEnquiry;
import com.scb.model.PersistanceData;
import com.scb.model.ProcessFlowSequence;
import com.scb.model.RequestData;
import com.scb.model.ResponseMessage;
import com.scb.repository.OutwardBalanceEnquiryRepository;
import com.scb.service.CustomerRequestService;
import com.scb.utils.CommonConstants;
import com.scb.utils.RequestParser;
import com.scb.utils.SCBCommonMethods;
import com.scb.utils.XmlParser;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CustomerRequestServiceImpl implements CustomerRequestService {
	@Autowired
	private SCBCommonMethods commonMethods;
	@Autowired
	private BalanceEnquiryConfig propertiesConfig;
	@Autowired
	private GcgInternalApiCall gcgInternalApiCall;
	@Autowired
	private JMSCorrelationalConfig jmsCorrelationalConfig;
	
	@Autowired 
	private OutwardBalanceEnquiryRepository outwardBalanceEnquiryRepository;

	@Override
	public BalanceEnquiryResponse customerRequestHandleService(BalanceEnquiryRequest customerRequest) {
		BalanceEnquiry customerRequestData = commonMethods.getBalanceEnquiryFromRequest(customerRequest);
		ResponseEntity<MsAuditLog> msAuditLogApiResponse = null;
		if (propertiesConfig.getIsEnableAuditLog().equalsIgnoreCase("yes")) {
			MsAuditLog parseAuditLog = commonMethods.getAuditLogDetails(customerRequestData);
			msAuditLogApiResponse = gcgInternalApiCall.msAuditLogApiCall(parseAuditLog);
		}
		
		ResponseEntity<BalanceEnquiryResponse> customerResponseFromPersistDb = null;
		ResponseEntity<BalanceEnquiryResponse> customerResponseFromvalidator = gcgInternalApiCall.msValidatorCall(customerRequestData);
		String downstreamProtocol = customerResponseFromvalidator.getBody().getResponseMessage();
		log.debug("Down stream protocol: .."+downstreamProtocol);
		if(customerResponseFromvalidator.getBody().getResponseCode() == 200){
			customerResponseFromPersistDb = gcgInternalApiCall
					.msCustomerPersistApiCall(customerRequestData);
		}else{
			return customerResponseFromvalidator.getBody();
		}
		
	//	ResponseEntity<CustomerResponse> customerResponseFromPersistDb = gcgInternalApiCall.msCustomerPersistApiCall(customerRequestData);
		ResponseEntity<BalanceEnquiryResponse> customerResponseFromDownStream = null;
		if (customerResponseFromPersistDb.getBody().getResponseCode() == 200) {
			
			if(downstreamProtocol.trim().equalsIgnoreCase("JMS")){
				log.debug("JMS call ");
				BalanceEnquiry customerRequestDataFromJMS = jmsCorrelationalConfig.send(customerResponseFromPersistDb.getBody().getCustomerRequestData());
				return commonMethods.getSuccessResponse(customerRequestDataFromJMS, "Successful response from JMS");
			} else if(downstreamProtocol.trim().equalsIgnoreCase("HTTP")){
				customerResponseFromDownStream = gcgInternalApiCall
						.msDownStreamCall(customerResponseFromPersistDb.getBody().getCustomerRequestData());
			}
			else{
				customerResponseFromDownStream = gcgInternalApiCall
						.msDownStreamCall(customerResponseFromPersistDb.getBody().getCustomerRequestData());
			}
			
		} else {
			return customerResponseFromPersistDb.getBody();
		}
		
		return  customerResponseFromDownStream.getBody();
	}
	
	@Override
	public BalanceEnquiryResponse requestHandleService(Map<String, String> requestHeader, String request) {
		String contentType = requestHeader.get("content-type");

		OutwardBalanceEnquiry balanceEnquiry = null;
		if (CommonConstants.APPLICATION_XML.equalsIgnoreCase(contentType)) {
			RequestParser parser = new XmlParser();
			balanceEnquiry = (OutwardBalanceEnquiry) parser.parse(request);
		}
		
		BalanceEnquiryResponse 	enquiryReponse = new BalanceEnquiryResponse();
		
		//save transaction in database
		boolean status = false;
		if(balanceEnquiry != null) {
			status = saveTrancation(balanceEnquiry);
		} else {
			log.info("Invalid payload format, could not parse payload.");
			enquiryReponse.setResponseCode(500);
			enquiryReponse.setResponseMessage("Invalid payload format, could not parse payload");
			return enquiryReponse;
		}
		
		ResponseEntity<ResponseMessage> configResponse = null;
		if (propertiesConfig.getConfigServiceURL() != null ) {
			configResponse = gcgInternalApiCall.configServiceApiCall(getRequestData(balanceEnquiry), 
					propertiesConfig.getConfigServiceURL());
		}
		
		ResponseEntity<BalanceEnquiryResponse> serviceResponse = null;
		
		ResponseMessage services = configResponse.getBody();
		//List<ProcessFlowSequence> serviceList = null;
		
		if (services.getResponseCode() != 200) {
			return commonMethods.getErrorResponse();
		}
		
		String processes = services.getProcesses();
		List<String> serviceList = new ArrayList<String>();
		if(processes != null) {
			StringTokenizer st = new StringTokenizer(processes, "|");
	        while (st.hasMoreTokens())
	        	serviceList.add(st.nextToken());
		}
		//serviceList = new ArrayList();//services.getList();
		log.info("ProcessFlowSequences List : " + serviceList);
		
		for (int i = 0; i < serviceList.size(); i++) {
			//ProcessFlowSequence service = serviceList.get(i);
			String service = serviceList.get(i);
			
			if (service.contains("persistence")) {
				if (propertiesConfig.getPersistenceServiceURL() != null ) {
					//MsAuditLog parseAuditLog = commonMethods.getAuditLogDetails(customerRequestData);
					
					serviceResponse = gcgInternalApiCall.persistenceServiceApiCall(getParisitenceData(balanceEnquiry));
				}
				
				log.info("Persistence Service Response : " + serviceResponse);
				
				//transaction saved, then get services list  be invoked
				if(serviceResponse != null && serviceResponse.getBody() != null 
						&& serviceResponse.getBody().getResponseCode() == 201) {
					balanceEnquiry.setStatus("STORED");
					balanceEnquiry.setMessage("Message persisted successfully");
					log.info("Calling update transction status for persistence service call : " + serviceResponse.getBody());
					status = saveTrancation(balanceEnquiry);
				} else {
					balanceEnquiry.setStatus("FAILURE TO STORE");
					balanceEnquiry.setMessage(serviceResponse.getBody().getResponseMessage());
					status = saveTrancation(balanceEnquiry);
					return serviceResponse.getBody();
				}
			} else {
				log.info("Calling " + service + "....");
				serviceResponse = gcgInternalApiCall.serviceApiCall(getRequestData(balanceEnquiry), service);
				
				log.info(service + " Response : " + serviceResponse);
				if(serviceResponse != null && serviceResponse.getBody() != null 
						&& serviceResponse.getBody().getResponseCode() == 200) {
					log.info("Calling update transction status for " + service + " call : "  + serviceResponse.getBody());
					balanceEnquiry.setStatus("SUCCESS");
					balanceEnquiry.setMessage(service + " call successfully");
					status = saveTrancation(balanceEnquiry);
				} else {
					log.info("Calling " + service + " failed : "  + serviceResponse.getBody());
					balanceEnquiry.setStatus("FAILED");
					balanceEnquiry.setMessage(serviceResponse.getBody().getResponseMessage());
					status = saveTrancation(balanceEnquiry);
					return serviceResponse.getBody();
				}

			}
		}
			
	
//		if (propertiesConfig.getPersistenceServiceURL() != null ) {
//			//MsAuditLog parseAuditLog = commonMethods.getAuditLogDetails(customerRequestData);
//			
//			serviceResponse = gcgInternalApiCall.persistenceServiceApiCall(getParisitenceData(balanceEnquiry));
//		}
//		
//		log.info("Persistence Service Response : " + serviceResponse);
//		
//		//transaction saved, then get services list to be invoked
//		if(serviceResponse != null && serviceResponse.getBody() != null 
//				&& serviceResponse.getBody().getResponseCode() == 201) {
//			balanceEnquiry.setStatus("STORED");
//			balanceEnquiry.setMessage("Message persisted successfully");
//			log.info("Calling update transction status for persistence service call : " + serviceResponse.getBody());
//			status = saveTrancation(balanceEnquiry);
//		} else {
//			balanceEnquiry.setStatus("FAILURE TO STORE");
//			balanceEnquiry.setMessage(serviceResponse.getBody().getResponseMessage());
//			status = saveTrancation(balanceEnquiry);
//			return serviceResponse.getBody();
//		}
//		
//		//call next service
//		if (propertiesConfig.getConformityCheckServiceURL() != null ) {
//			log.info("Calling ConformityCheck service....");
//			serviceResponse = gcgInternalApiCall.serviceApiCall(getRequestData(balanceEnquiry), propertiesConfig.getConformityCheckServiceURL());
//		}
//		
//		log.info("ConformityCheck Service Response : " + serviceResponse);
//		if(serviceResponse != null && serviceResponse.getBody() != null 
//				&& serviceResponse.getBody().getResponseCode() == 200) {
//			log.info("Calling update transction status for conformity service call : "  + serviceResponse.getBody());
//			balanceEnquiry.setStatus("VALID MESSAGE");
//			balanceEnquiry.setMessage("Message validated successfully");
//		} else {
//			balanceEnquiry.setStatus("VALIDATION FAILED");
//			balanceEnquiry.setMessage(serviceResponse.getBody().getResponseMessage());
//			return serviceResponse.getBody();
//		}
//		
//		//call next service
//		if (propertiesConfig.getDupCheckServiceURL() != null ) {
//			log.info("Calling Dup Check service....");
//			serviceResponse = gcgInternalApiCall.serviceApiCall(getRequestData(balanceEnquiry), propertiesConfig.getDupCheckServiceURL());
//		}
//		
//		log.info("Dup Check Service Response : " + serviceResponse);
//		if(serviceResponse != null && serviceResponse.getBody() != null 
//				&& serviceResponse.getBody().getResponseCode() == 200) {
//			log.info("Calling update transction status for dup check service call : " + serviceResponse.getBody());
//			balanceEnquiry.setStatus("NOT DUPLICATE");
//			balanceEnquiry.setMessage("Message is not a duplicate");
//		} else {
//			balanceEnquiry.setStatus("DUPLICATE CHECK FAILED");
//			balanceEnquiry.setMessage(serviceResponse.getBody().getResponseMessage());
//			return serviceResponse.getBody();
//		}
//		
//		//call next service
//		if (propertiesConfig.getBusinessRuleServiceURL() != null ) {
//			log.info("Calling Business Rule service....");
//			serviceResponse = gcgInternalApiCall.serviceApiCall(getRequestData(balanceEnquiry), propertiesConfig.getBusinessRuleServiceURL());
//		}
//		
//		log.info("Business Rule Service Response : " + serviceResponse);
//		if(serviceResponse != null && serviceResponse.getBody() != null 
//				&& serviceResponse.getBody().getResponseCode() == 200) {
//			log.info("Calling update transction status for business rule service call : " + serviceResponse.getBody());
//			balanceEnquiry.setStatus("COMPLIENT TO BUSINESS RULES");
//			balanceEnquiry.setMessage("Message validated against business rules successfully");
//		} else {
//			balanceEnquiry.setStatus("BUSINESS VALIDATION FAILED");
//			balanceEnquiry.setMessage(serviceResponse.getBody().getResponseMessage());
//			return serviceResponse.getBody();
//		}
//
//		//call next service
//		if (propertiesConfig.getTransmitterServiceURL() != null ) {
//			log.info("Calling Transmitter service....");
//			serviceResponse = gcgInternalApiCall.serviceApiCall(getRequestData(balanceEnquiry), propertiesConfig.getTransmitterServiceURL());
//		}
//		
//		log.info("Transmitter Service Response : " + serviceResponse);
//		if(serviceResponse != null && serviceResponse.getBody() != null 
//				&& serviceResponse.getBody().getResponseCode() == 200) {
//			log.info("Calling update transction status for transmitter service call : " + serviceResponse.getBody());
//			balanceEnquiry.setStatus("RESPONSE RECEIVED");
//			balanceEnquiry.setMessage("Successfully received response from downstream application");
//		} else {
//			balanceEnquiry.setStatus("CONNECTION FAILED");
//			balanceEnquiry.setMessage(serviceResponse.getBody().getResponseMessage());
//			return serviceResponse.getBody();
//		}		
		return serviceResponse.getBody();
	}

	public boolean saveTrancation(OutwardBalanceEnquiry balanceEnquiry) {
		log.info("Transaction received: " + balanceEnquiry);
		OutwardBalanceEnquiry balanceEnquiryEntity = null;
		List<OutwardBalanceEnquiry> balanceEnquiryEntityList = null;
		try {
			balanceEnquiryEntityList = outwardBalanceEnquiryRepository.findByTransactionId(balanceEnquiry.getTransactionID());
		} catch (NoSuchElementException ex) {
			log.info("Error in finding transaction" + ex.getMessage());
		}
		
		if (balanceEnquiryEntityList != null && balanceEnquiryEntityList.size() > 0) {
			return false;
		} else {
			log.info("Transaction deatils being saved in db");

			outwardBalanceEnquiryRepository.save(balanceEnquiry);
			log.info("transaction saved in db");
			return true;
		}
	}
	
	private PersistanceData getParisitenceData(OutwardBalanceEnquiry balanceEnquiry) {
		PersistanceData persistenceData = PersistanceData.builder().transactionID(balanceEnquiry.getTransactionID())
				.transactionType(balanceEnquiry.getTransactionType())
				.transactionSubType(balanceEnquiry.getTransactionSubType())
				.payloadFormat(balanceEnquiry.getPayloadFormat())
				.payload(balanceEnquiry.getPayload())
				.createdOn(balanceEnquiry.getCreatedOn())
				.updatedOn(balanceEnquiry.getUpdatedOn()).build();
		return persistenceData;	
	}
	
	private RequestData getRequestData(OutwardBalanceEnquiry balanceEnquiry) {
		RequestData requestData = RequestData.builder().transactionID(balanceEnquiry.getTransactionID())
				.transactionType(balanceEnquiry.getTransactionType())
				.transactionSubType(balanceEnquiry.getTransactionSubType())
				.payloadFormat(balanceEnquiry.getPayloadFormat())
				.payload(balanceEnquiry.getPayload())
				.createdOn(balanceEnquiry.getCreatedOn())
				.updatedOn(balanceEnquiry.getUpdatedOn()).build();
		return requestData;	
	}
}
