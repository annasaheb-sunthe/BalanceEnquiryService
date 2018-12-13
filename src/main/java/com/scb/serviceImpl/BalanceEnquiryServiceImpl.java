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
import com.scb.model.BalanceEnquiryResponse;
import com.scb.model.OutwardBalanceEnquiry;
import com.scb.model.PersistanceData;
import com.scb.model.RequestData;
import com.scb.model.ResponseMessage;
import com.scb.repository.OutwardBalanceEnquiryRepository;
import com.scb.service.BalanceEnquiryService;
import com.scb.utils.CommonConstants;
import com.scb.utils.RequestParser;
import com.scb.utils.SCBCommonMethods;
import com.scb.utils.XmlParser;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class BalanceEnquiryServiceImpl implements BalanceEnquiryService {
	@Autowired
	private SCBCommonMethods commonMethods;
	
	@Autowired
	private BalanceEnquiryConfig propertiesConfig;
	
	@Autowired
	private GcgInternalApiCall gcgInternalApiCall;
	
	@Autowired 
	private OutwardBalanceEnquiryRepository outwardBalanceEnquiryRepository;

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
		log.info("ProcessFlowSequences List : " + serviceList);
		
		for (int i = 0; i < serviceList.size(); i++) {
			String service = serviceList.get(i);
			
			if (service.contains("persistence")) {
				if (propertiesConfig.getPersistenceServiceURL() != null ) {
				
					serviceResponse = gcgInternalApiCall.persistenceServiceApiCall(getParisitenceData(balanceEnquiry));
				}
				
				log.info("Persistence Service Response : " + serviceResponse.getStatusCode());
				
				//transaction saved, then get services list  be invoked
				if(serviceResponse != null && serviceResponse.getBody() != null 
						&& serviceResponse.getBody().getResponseCode() == 201) {
					balanceEnquiry.setStatus("STORED");
					balanceEnquiry.setMessage("Message persisted successfully");
					log.info("Calling update transction status for persistence service call : " + serviceResponse.getBody().getResponseCode());
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
					log.info("Calling update transction status for " + service + " call : "  + serviceResponse.getBody().getResponseCode());
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
			
		return serviceResponse.getBody();
	}

	public boolean saveTrancation(OutwardBalanceEnquiry balanceEnquiry) {
		log.info("Transaction received - TransactionType: " + balanceEnquiry.getTransactionType() 
			+ ", trnsactionSubType : " + balanceEnquiry.getTransactionSubType()
			+ ", payloadFormat : " + balanceEnquiry.getPayloadFormat());
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
