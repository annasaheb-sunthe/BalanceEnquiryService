package com.scb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @ToString @XmlRootElement
public class ProcessFlowSequence implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@EmbeddedId
	private ProcessFlowSequenceCompositeKey processflowsequencecompositekey;
	@Column(name="serviceName",updatable=false)
	private String serviceName;
	@Column(name="processName",updatable=false)
	private String processName;
	@Column(name="createdon",updatable=false)
	@CreationTimestamp
	private LocalDateTime createdOn;
	@Column(name="updatedon")
	@UpdateTimestamp
	private LocalDateTime updatedOn;
	@Column(name="sequence")
	private long sequence;
	@Column(name="url")
	private String url;
}
