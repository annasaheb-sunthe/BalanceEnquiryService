package com.scb.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString @XmlRootElement(name="Group")
@Embeddable
public class ProcessFlowSequenceCompositeKey implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="processid",nullable=false)
	private long processId;
	@Column(name="serviceid",nullable=false)
	private long serviceId;
}
