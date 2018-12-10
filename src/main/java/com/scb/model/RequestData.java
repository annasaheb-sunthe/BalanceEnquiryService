package com.scb.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @ToString @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD) 
public class RequestData implements Serializable {
	private static final long serialVersionUID = 1125749775288601451L;
	private long transactionID;
	private String transactionType;
	private String transactionSubType;
	private String payloadFormat;
	private String payload;
	private String createdOn;
	private String updatedOn;
}
