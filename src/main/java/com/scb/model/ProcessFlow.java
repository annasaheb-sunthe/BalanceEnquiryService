package com.scb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter @Setter @Builder @Entity  @Table(name="processflow") @NoArgsConstructor @AllArgsConstructor @ToString @XmlRootElement
public class ProcessFlow implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private long processId;
	@Column(name="processName",unique=true,nullable=false)
	private String processName;
	private String description;
	@Column(name="status")
	private int status;
	@Column(name="statusName",nullable=false)
	private String statusName;
	@Column(name="countrycode")
	private String countryCode;
	@Column(name="createdon",updatable=false)
	@CreationTimestamp
	private LocalDateTime createdOn;
	@Column(name="updatedon")
	@UpdateTimestamp
	private LocalDateTime updatedOn;
}
