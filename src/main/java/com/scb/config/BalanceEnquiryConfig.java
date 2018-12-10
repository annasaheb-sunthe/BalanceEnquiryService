package com.scb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Component
@Getter
public class BalanceEnquiryConfig {

	@Value("${GCG.downStreamURL}")
	private String downStreamURL;

	@Value("${GCG.auditLogURL}")
	private String auditLogURL;

	@Value("${GCG.errorLogURL}")
	private String errorLogURL;

	@Value("${GCG.customerRequestPersist}")
	private String customerRequestPersistURL;
	
	@Value("${GCG.customerValidator}")
	private String customerValidatorURL;
	
	@Value("${GCG.enableAuditLog}")
	private String isEnableAuditLog;
	
	@Value("${GCG.downstreamCallConfig}")
	private String downstreamCallConfig;
	
	@Value("${GCG.jmsTemplateTimeout}")
	private int jmsTemplateTimeout;
	
	@Value("${GCG.jmsRequestQueue}")
	private String jmsRequestQueue;
	
	@Value("${GCG.jmsResponseQueue}")
	private String jmsResponseQueue;
	
	@Value("${msbif.lti.persistenceServiceURL}")
	private String persistenceServiceURL;
	
	@Value("${msbif.lti.conformityCheckServiceURL}")
	private String conformityCheckServiceURL;
	
	@Value("${msbif.lti.configServiceURL}")
	private String configServiceURL;
	
	@Value("${msbif.lti.dupCheckServiceURL}")
	private String dupCheckServiceURL;
	
	@Value("${msbif.lti.businessRuleServiceURL}")
	private String businessRuleServiceURL;
	
	@Value("${msbif.lti.transformServiceURL}")
	private String transformServiceURL;
	
	@Value("${msbif.lti.transmitterServiceURL}")
	private String transmitterServiceURL;
	
	@Value("${msbif.lti.auditLogServiceURL}")
	private String auditLogServiceURL;
	
	@Value("${msbif.lti.notificationServiceURL}")
	private String notificationServiceURL;
}
