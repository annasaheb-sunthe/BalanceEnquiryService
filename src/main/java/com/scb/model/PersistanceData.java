package com.scb.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @Builder @Entity @Table(name="persistantdata") @NoArgsConstructor @AllArgsConstructor @ToString @XmlRootElement
public class PersistanceData {

	private static final long serialVersionUID = 1L;
	@Id
	private long transactionID;
	private String transactionType;
	private String transactionSubType;
	private String payloadFormat;
	private String payload;
	private String createdOn;
	private String updatedOn;
}
