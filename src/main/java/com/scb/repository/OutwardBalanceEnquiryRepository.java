package com.scb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.scb.model.OutwardBalanceEnquiry;

//@RepositoryRestResource
public interface OutwardBalanceEnquiryRepository extends JpaRepository<OutwardBalanceEnquiry, OutwardBalanceEnquiry> {
	@Query(value="SELECT * FROM BALANCE_ENQUIRY_TABLE BET WHERE BET.transactionID = ?1, BET.transactionType?2, BET.transactionSubType?3",nativeQuery=true)
	List<OutwardBalanceEnquiry> findByTransactionIdTypeAndSubType(long transactionID, String transactionType, String transactionSubType);
	
	@Query(value="SELECT * FROM BALANCE_ENQUIRY_TABLE WHERE transactionID = ?1", nativeQuery=true)
	List<OutwardBalanceEnquiry> findByTransactionId(long transactionID);
}
