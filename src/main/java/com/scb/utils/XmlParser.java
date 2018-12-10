package com.scb.utils;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.scb.model.OutwardBalanceEnquiry;

public class XmlParser implements RequestParser {
	@Autowired
	private SCBCommonMethods commonMethods;

	@Override
	public OutwardBalanceEnquiry parse(Object request) {
		
		OutwardBalanceEnquiry balanceEnquiry = null;
		
		try {
	         //File inputFile = new File("input.txt");
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder;

	         dBuilder = dbFactory.newDocumentBuilder();

	         Document doc = dBuilder.parse(new InputSource(new StringReader(request.toString())));
	         
	         doc.getDocumentElement().normalize();

	         XPath xPath =  XPathFactory.newInstance().newXPath();

	         
	         //get message type
	         String msgTypeExp = "//Document/FIToFICstmrCdtTrf/GrpHdr/MsgId";//"//sendFastCustomerCreditTransferRequest/header/messageDetails/messageType/typeName";
	         
	         //get source system
	         String captureSystemExp = "//Document/FIToFICstmrCdtTrf/GrpHdr/SttlmInf/ClrSys";//"//sendFastCustomerCreditTransferRequest/header/captureSystem";
	         
	         //get payload format
	         String payloadFormatExp = "//sendFastCustomerCreditTransferRequest/sendFastCustomerCreditTransferReqPayload/payloadFormat";
	         
	         //get payload version
	         String payloadVersionExp = "//sendFastCustomerCreditTransferRequest/sendFastCustomerCreditTransferReqPayload/payloadVersion";
	         
	         //get country code
	         String countryCodeExp = "//Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/Ctry";
	         		//sendFastCustomerCreditTransferRequest/sendFastCustomerCreditTransferReqPayload/sendFastCustomerCreditTransferReq/FIToFICstmrCdtTrf01/Header/Fr/FIId/FinInstnId/PstlAdr/Ctry";
	         
	         //extract message type
	         NodeList msgTypeList = (NodeList) xPath.compile(msgTypeExp).evaluate(doc, XPathConstants.NODESET);
	         
	         String messageType = null;
	         
	         if(msgTypeList.getLength() > 0) {
	        	 Node nNode = msgTypeList.item(0);
	            System.out.println("\nmsgType - Current Element :" + nNode.getNodeName());
	            
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		               Element eElement = (Element) nNode;
		               System.out.println("Message Type [" + 0 + "] : "
		 	                  + nNode.getTextContent());
		               messageType = nNode.getTextContent();
	            }
	         }
	         
	         //extract source system
	         NodeList captureSystemList = (NodeList) xPath.compile(captureSystemExp).evaluate(doc, XPathConstants.NODESET);
	         String sourceSystem = null;
	         
	         if(captureSystemList.getLength() > 0) {
	        	 Node nNode = captureSystemList.item(0);
	            System.out.println("\ncaptureSystemList - Current Element :" + nNode.getNodeName());
	            
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		               Element eElement = (Element) nNode;
		               System.out.println("CaptureSystem [" + 0 + "] : "
		 	                  + nNode.getTextContent());
		               sourceSystem = nNode.getTextContent();
	            }
	         }
	         
	         //extract countryCodeExp
	         NodeList countryCodeList = (NodeList) xPath.compile(countryCodeExp).evaluate(doc, XPathConstants.NODESET);
	         String countryCode = null;
	         
	         if(countryCodeList.getLength() > 0) {
	        	 Node nNode = countryCodeList.item(0);
	            System.out.println("\ncountryCodeList - Current Element :" + nNode.getNodeName());
	            
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		               Element eElement = (Element) nNode;
		               System.out.println("Country Code [" + 0 + "] : "
		 	                  + nNode.getTextContent());
		               countryCode = nNode.getTextContent();
	            }
	         }

	         //extract payloadFormat
	  //       NodeList payloadFormatList = (NodeList) xPath.compile(payloadFormatExp).evaluate(doc, XPathConstants.NODESET);
	         String payloadFormat = "XML";
	         
//	         if(payloadFormatList.getLength() > 0) {
//	        	 Node nNode = payloadFormatList.item(0);
//	            System.out.println("\npayloadFormatList - Current Element :" + nNode.getNodeName());
//	            
//	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//		               Element eElement = (Element) nNode;
//		               System.out.println("Payload Format [" + 0 + "] : "
//		 	                  + nNode.getTextContent());
//		               payloadFormat = nNode.getTextContent();
//	            }
//	         }
	         
	         if (countryCode.equalsIgnoreCase("IN")) {
	        	 countryCode = "Ind";
	         } else if(countryCode.equalsIgnoreCase("US")) {
	        	 countryCode = "Usa";
	         } else if (countryCode.equalsIgnoreCase("GR")) {
	        	 countryCode = "Ger";
	         }
	         
	         balanceEnquiry = OutwardBalanceEnquiry.builder().transactionID(getTransactionId()).transactionType("OutwardCreditTransfer")
	        		 .transactionType("OutwardCreditTransfer")
	        		 .transactionSubType(countryCode)
	        		 .payloadFormat(payloadFormat)
	        		 .sourceSystem(sourceSystem)
	        		 .payload(request.toString())
	        		 .status("RECEIVED")
	        		 .createdOn(getCurrentDateTime())
	        		 .updatedOn(getCurrentDateTime()).build();
	        		 
	         //System.out.println("OutwardBalanceEnquiry : " + balanceEnquiry.toString());
	        
//	         String expression = "/class/student";	        
//	         NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
//	            doc, XPathConstants.NODESET);
//
//	         for (int i = 0; i < nodeList.getLength(); i++) {
//	            Node nNode = nodeList.item(i);
//	            System.out.println("\nCurrent Element :" + nNode.getNodeName());
//	            
//	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//	               Element eElement = (Element) nNode;
//	               System.out.println("Student roll no :" + eElement.getAttribute("rollno"));
//	               System.out.println("First Name : " 
//	                  + eElement
//	                  .getElementsByTagName("firstname")
//	                  .item(0)
//	                  .getTextContent());
//	               System.out.println("Last Name : " 
//	                  + eElement
//	                  .getElementsByTagName("lastname")
//	                  .item(0)
//	                  .getTextContent());
//	               System.out.println("Nick Name : " 
//	                  + eElement
//	                  .getElementsByTagName("nickname")
//	                  .item(0)
//	                  .getTextContent());
//	               System.out.println("Marks : " 
//	                  + eElement
//	                  .getElementsByTagName("marks")
//	                  .item(0)
//	                  .getTextContent());
//	            }
//	         }
	      } catch (ParserConfigurationException e) {
	         e.printStackTrace();
	      } catch (SAXException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (XPathExpressionException e) {
	         e.printStackTrace();
	      }
		 return balanceEnquiry;
	}

	public String getCurrentDateTime() {
		LocalDateTime localDateTime = LocalDateTime.now();
		return localDateTime.toString();
	}
	
	public long getTransactionId() {
		Random random = new Random(System.nanoTime() % 100000);
		long uniqueTansactionId = random.nextInt(1000000000);
		return uniqueTansactionId;
	}
}
