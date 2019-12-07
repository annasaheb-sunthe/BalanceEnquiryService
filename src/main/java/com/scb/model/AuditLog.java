package com.scb.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @Builder @Entity @Table(name="AuditLog") @NoArgsConstructor @AllArgsConstructor @ToString @XmlRootElement
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column
	private long auditLogId;
	@Column
	private long transactionId;
	@Column
	private String transactionType;
	@Column
	private String transactionSubType;
	@Column
	private String serviceName;
	@Column
	private String messageType;
//	@Column()
//	private Date timestamp;
	@Basic
	@Temporal(TemporalType.DATE)
	private Date timestamp;
	@Column
	private String status;
	@Column(length=1000)
	private String message;
	
//	public void setTimestamp(String date) {
//		try {
//			this.timestamp = new SimpleDateFormat("yyyy-MM-dd").parse(date);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
