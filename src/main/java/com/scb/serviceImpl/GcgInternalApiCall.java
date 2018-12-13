package com.scb.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.scb.config.BalanceEnquiryConfig;
import com.scb.model.AuditLog;
import com.scb.model.BalanceEnquiry;
import com.scb.model.BalanceEnquiryResponse;
import com.scb.model.BalanceEnquiryValidateResponse;
import com.scb.model.MsAuditLog;
import com.scb.model.MsErrorLog;
import com.scb.model.PersistanceData;
import com.scb.model.ProcessFlowSequence;
import com.scb.model.RequestData;
import com.scb.model.ResponseMessage;
import com.scb.utils.SCBCommonMethods;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GcgInternalApiCall {

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private BalanceEnquiryConfig customerConfig;
	@Autowired
	private SCBCommonMethods commonMethods;
	
	public ResponseEntity<BalanceEnquiryResponse> persistenceServiceApiCall(PersistanceData persistanceData) {
		//log.info("OutwardBalanceEnquiry in persistence service: " + persistanceData);
		ResponseEntity<BalanceEnquiryResponse> persistenceServiceApi = null;
		ResponseEntity<ResponseMessage> reponseMessage = null;
		try {
			HttpEntity<PersistanceData> entity = new HttpEntity<PersistanceData>(persistanceData);
			log.info("calling restTemplate...");
			reponseMessage = restTemplate.exchange(customerConfig.getPersistenceServiceURL(),
					HttpMethod.POST, entity, ResponseMessage.class);
			log.info("reponseMessage : " + reponseMessage);
			BalanceEnquiryResponse br = new BalanceEnquiryResponse().builder().responseCode(reponseMessage.getBody().getResponseCode()).responseMessage(reponseMessage.getBody().getResponseMessage()).build();
			persistenceServiceApi = new ResponseEntity<BalanceEnquiryResponse>(br, HttpStatus.CREATED);
			log.info("Response Entity [persistenceServiceApi] : " + persistenceServiceApi);
		} catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			log.info("HttpClientErrorException | HttpServerErrorException occured.... : " + httpClientOrServerEx.getMessage());
			httpClientOrServerEx.printStackTrace();
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			msErrorLog.setUuid(persistanceData.getTransactionID());
			msErrorLog.setTimeStamp(persistanceData.getCreatedOn());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling persister api"), HttpStatus.BAD_GATEWAY);
				//return responseOfCustomerApi;
				// retry logic goes here
			} else {
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling persister api"), HttpStatus.BAD_GATEWAY);
				
				// do something
			}
		} catch (Exception e) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			msErrorLog.setUuid(persistanceData.getTransactionID());
			msErrorLog.setTimeStamp(persistanceData.getCreatedOn());
			msErrorLogApiCall(msErrorLog);
			return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling persister api"), HttpStatus.BAD_GATEWAY);
			
		}
		return persistenceServiceApi;
	}

	public ResponseEntity<BalanceEnquiryResponse> serviceApiCall(RequestData requestData, String serviceURL) {
		//log.info("RequestData in serviceApiCall: " + requestData);
		log.info("RequestData in serviceURL: " + serviceURL);
		ResponseEntity<BalanceEnquiryResponse> serviceApiResponse = null;
		ResponseEntity<ResponseMessage> reponseMessage = null;
		try {
			HttpEntity<RequestData> entity = new HttpEntity<RequestData>(requestData);
			log.info("calling restTemplate...");
			reponseMessage = restTemplate.exchange(serviceURL, HttpMethod.POST, entity, ResponseMessage.class);
			log.info("reponseMessage : " + reponseMessage);
			BalanceEnquiryResponse br = new BalanceEnquiryResponse().builder().responseCode(reponseMessage.getBody().getResponseCode()).responseMessage(reponseMessage.getBody().getResponseMessage()).build();
			serviceApiResponse = new ResponseEntity<BalanceEnquiryResponse>(br, HttpStatus.OK);
			log.info("Response Entity [serviceApiResponse] : " + serviceApiResponse);
		} catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			log.info("HttpClientErrorException | HttpServerErrorException occured.... : " + httpClientOrServerEx.getMessage());
			httpClientOrServerEx.printStackTrace();
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			msErrorLog.setUuid(requestData.getTransactionID());
			msErrorLog.setTimeStamp(requestData.getCreatedOn());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling serviceApiCall api"), HttpStatus.BAD_GATEWAY);
			} else {
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling serviceApiCall api"), HttpStatus.BAD_GATEWAY);
			}
		} catch (Exception e) {
			log.info("Exception occured.... : " + e.getMessage());
			e.printStackTrace();
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			msErrorLog.setUuid(requestData.getTransactionID());
			msErrorLog.setTimeStamp(requestData.getCreatedOn());
			msErrorLogApiCall(msErrorLog);
			return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling serviceApiCall api"), HttpStatus.BAD_GATEWAY);
		}
		return serviceApiResponse;
	}

	public ResponseEntity<ResponseMessage> configServiceApiCall(RequestData requestData, String serviceURL) {
		//log.info("RequestData in serviceApiCall: " + requestData);
		log.info("RequestData in serviceURL: " + serviceURL);
		ResponseEntity<List<ProcessFlowSequence>> serviceApiResponse = null;
		ResponseEntity<ResponseMessage> reponseMessage = null;
		//ResponseMessage reponseMessage = null;
		List processesList = null;
		
		try {
			HttpEntity<RequestData> entity = new HttpEntity<RequestData>(requestData);
			log.info("calling restTemplate...");
			reponseMessage = restTemplate.exchange(serviceURL, HttpMethod.POST, entity, ResponseMessage.class);
			log.info("reponseMessage : " + reponseMessage);
			//BalanceEnquiryResponse br = new BalanceEnquiryResponse().builder().responseCode(reponseMessage.getBody().getResponseCode()).responseMessage(reponseMessage.getBody().getResponseMessage()).build();
			//serviceApiResponse = new ResponseEntity<List<ProcessFlowSequence>>((List<ProcessFlowSequence>) reponseMessage.getBody().getList(), HttpStatus.OK);
			log.info("Response Entity [serviceApiResponse] : " + serviceApiResponse);
		} catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			log.info("HttpClientErrorException | HttpServerErrorException occured.... : " + httpClientOrServerEx.getMessage());
			httpClientOrServerEx.printStackTrace();
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			msErrorLog.setUuid(requestData.getTransactionID());
			msErrorLog.setTimeStamp(requestData.getCreatedOn());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				return new ResponseEntity<ResponseMessage>( commonMethods.getResponseMessage("Problem While calling serviceApiCall api"), HttpStatus.BAD_GATEWAY);
			} else {
				return new ResponseEntity<ResponseMessage>( commonMethods.getResponseMessage("Problem While calling serviceApiCall api"), HttpStatus.BAD_GATEWAY);
			}
		} catch (Exception e) {
			log.info("Exception occured.... : " + e.getMessage());
			e.printStackTrace();
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			msErrorLog.setUuid(requestData.getTransactionID());
			msErrorLog.setTimeStamp(requestData.getCreatedOn());
			msErrorLogApiCall(msErrorLog);
			return new ResponseEntity<ResponseMessage>( commonMethods.getResponseMessage("Problem While calling serviceApiCall api"), HttpStatus.BAD_GATEWAY);
		}
		return reponseMessage;
	}

	public ResponseEntity<MsAuditLog> msAuditLogApiCall(MsAuditLog auditLog) {
		ResponseEntity<MsAuditLog> responseAuditLog = null;
		try {
			log.debug("GCG internal call audit");
			HttpEntity<MsAuditLog> entity = new HttpEntity<MsAuditLog>(auditLog);
			responseAuditLog = restTemplate.exchange(customerConfig.getAuditLogURL(), HttpMethod.POST, entity,
					MsAuditLog.class);
		} catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			msErrorLog.setUuid(auditLog.getUuid());
			msErrorLog.setTimeStamp(auditLog.getTimeStamp());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				// retry logic goes here
			} else {
				// do something
			}
		} catch (Exception e) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			msErrorLog.setUuid(auditLog.getUuid());
			msErrorLog.setTimeStamp(auditLog.getTimeStamp());
			msErrorLogApiCall(msErrorLog);
		}
		return responseAuditLog;
	}

	public void msErrorLogApiCall(MsErrorLog msErrorLog) {
		try {
			restTemplate.postForObject(customerConfig.getErrorLogURL(), msErrorLog, MsErrorLog.class);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public ResponseEntity<BalanceEnquiryResponse> msCustomerPersistApiCall(BalanceEnquiry customerRequestData) {
		ResponseEntity<BalanceEnquiryResponse> responseOfCustomerApi = null;
		try {
			HttpEntity<BalanceEnquiry> entity = new HttpEntity<BalanceEnquiry>(customerRequestData);
			responseOfCustomerApi = restTemplate.exchange(customerConfig.getCustomerRequestPersistURL(),
					HttpMethod.POST, entity, BalanceEnquiryResponse.class);
		} catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			msErrorLog.setUuid(customerRequestData.getTransactionId());
			msErrorLog.setTimeStamp(customerRequestData.getTimeStamp());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling persister api"), HttpStatus.BAD_GATEWAY);
				//return responseOfCustomerApi;
				// retry logic goes here
			} else {
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling persister api"), HttpStatus.BAD_GATEWAY);
				
				// do something
			}
		} catch (Exception e) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			msErrorLog.setUuid(customerRequestData.getTransactionId());
			msErrorLog.setTimeStamp(customerRequestData.getTimeStamp());
			msErrorLogApiCall(msErrorLog);
			return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling persister api"), HttpStatus.BAD_GATEWAY);
			
		}
		return responseOfCustomerApi;
	}

	public ResponseEntity<BalanceEnquiryResponse> msDownStreamCall(BalanceEnquiry customerRequestData) {
		ResponseEntity<BalanceEnquiry> responseOfCustomerApi = null;
		//ResponseEntity<CustomerRequestData> responseCustomerRequestData = null;
		try{
			HttpEntity<BalanceEnquiry> entity = new HttpEntity<BalanceEnquiry>(customerRequestData);
			responseOfCustomerApi = restTemplate.exchange(customerConfig.getDownStreamURL(), HttpMethod.POST, entity,
					BalanceEnquiry.class);
		}catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			msErrorLog.setUuid(customerRequestData.getTransactionId());
			msErrorLog.setTimeStamp(customerRequestData.getTimeStamp());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling downstream api"), HttpStatus.BAD_GATEWAY);
				
				// retry logic goes here
			} else {
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling downstream api"), HttpStatus.BAD_GATEWAY);
				
				// do something
			}
		} catch (Exception e) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			msErrorLog.setUuid(customerRequestData.getTransactionId());
			msErrorLog.setTimeStamp(customerRequestData.getTimeStamp());
			msErrorLogApiCall(msErrorLog);
			return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling downstream api"), HttpStatus.BAD_GATEWAY);
			
		}
		//return responseOfCustomerApi;
		log.debug("Downstream response: "+responseOfCustomerApi.getBody().toString());
		return new  ResponseEntity<BalanceEnquiryResponse>(commonMethods.getSuccessResponse(responseOfCustomerApi.getBody()), HttpStatus.OK);
	}
	
	public ResponseEntity<BalanceEnquiryResponse> msValidatorCall(BalanceEnquiry customerRequestData) {
		ResponseEntity<BalanceEnquiryValidateResponse> responseOfCustomerApi = null;
		//ResponseEntity<CustomerRequestData> responseCustomerRequestData = null;
		try{
			HttpEntity<BalanceEnquiry> entity = new HttpEntity<BalanceEnquiry>(customerRequestData);
			responseOfCustomerApi = restTemplate.exchange(customerConfig.getCustomerValidatorURL(), HttpMethod.POST, entity,
					BalanceEnquiryValidateResponse.class);
		}catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			msErrorLog.setUuid(customerRequestData.getTransactionId());
			msErrorLog.setTimeStamp(customerRequestData.getTimeStamp());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling validator api"), HttpStatus.BAD_GATEWAY);
				
				// retry logic goes here
			} else {
				return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse("Problem While calling validator api"), HttpStatus.BAD_GATEWAY);
				
				// do something
			}
		} catch (Exception e) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			msErrorLog.setUuid(customerRequestData.getTransactionId());
			msErrorLog.setTimeStamp(customerRequestData.getTimeStamp());
			msErrorLogApiCall(msErrorLog);
			return new ResponseEntity<BalanceEnquiryResponse>( commonMethods.getErrorResponse(500, e.getMessage()), HttpStatus.BAD_GATEWAY);
			
		}
		//return responseOfCustomerApi;
		log.debug("Downstream response: "+responseOfCustomerApi.getBody().toString());
		if(responseOfCustomerApi.getBody().getResponseCode() == 200){
			return new  ResponseEntity<BalanceEnquiryResponse>(commonMethods.getSuccessResponse(responseOfCustomerApi.getBody().getCustomerRequestData(), responseOfCustomerApi.getBody().getDownstream_protocol()), HttpStatus.OK);
		}else{
			return new  ResponseEntity<BalanceEnquiryResponse>(commonMethods.getErrorResponse(responseOfCustomerApi.getBody().getResponseCode(), responseOfCustomerApi.getBody().getResponseMessage()), HttpStatus.OK);
		}
	//	return new  ResponseEntity<CustomerResponse>(commonMethods.getSuccessResponse(responseOfCustomerApi.getBody().getCustomerRequestData()), HttpStatus.OK);
	}

	public ResponseEntity<AuditLog> auditLogApiCall(AuditLog auditLog) {
		ResponseEntity<AuditLog> responseAuditLog = null;
		try {
			log.debug("AuditLogService call...");
			HttpEntity<AuditLog> entity = new HttpEntity<AuditLog>(auditLog);
			responseAuditLog = restTemplate.exchange(customerConfig.getAuditLogServiceURL(), HttpMethod.POST, entity,
					AuditLog.class);
		} catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerEx) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(httpClientOrServerEx);
			msErrorLog.setErrorCode(httpClientOrServerEx.getStatusCode().toString());
			//msErrorLog.setUuid(auditLog.getUuid());
			msErrorLog.setTimeStamp(auditLog.getTimestamp().toString());
			if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpClientOrServerEx.getStatusCode())) {
				msErrorLogApiCall(msErrorLog);
				// retry logic goes here
			} else {
				// do something
			}
		} catch (Exception e) {
			MsErrorLog msErrorLog = commonMethods.getErrorLogDetails(e);
			//msErrorLog.setUuid(auditLog.getUuid());
			msErrorLog.setTimeStamp(auditLog.getTimestamp().toString());
			msErrorLogApiCall(msErrorLog);
		}
		return responseAuditLog;
	}	
}
