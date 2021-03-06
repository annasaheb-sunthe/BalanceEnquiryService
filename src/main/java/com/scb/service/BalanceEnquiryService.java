package com.scb.service;

import java.util.Map;

import com.scb.model.BalanceEnquiryRequest;
import com.scb.model.BalanceEnquiryResponse;
import com.scb.model.OutwardBalanceEnquiry;

public interface BalanceEnquiryService {
	public BalanceEnquiryResponse requestHandleService(Map<String, String> requestHeader, String request);

	public boolean saveTrancation(OutwardBalanceEnquiry balanceEnquiry);
}
