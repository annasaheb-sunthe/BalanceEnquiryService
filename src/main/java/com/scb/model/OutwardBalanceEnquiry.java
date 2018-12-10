package com.scb.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @Builder @Entity @Table(name="BALANCE_ENQUIRY_TABLE") @NoArgsConstructor @AllArgsConstructor @ToString @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD) 
public class OutwardBalanceEnquiry implements Serializable {
	
	private static final long serialVersionUID = 1125749775288601451L;
	@Id
	@Column
	private long transactionID;
	@Column
	private String transactionType;
	@Column
	private String transactionSubType;
	@Column
	private String payloadFormat;
	@Column
	private String sourceSystem;
	@Column (length=10000)
	private String payload;
	@Column
	private String status;
	@Column
	private String message;
	@Column 
	private String createdOn; //current timestamp
	@Column 
	private String updatedOn; //current timestamp
}
