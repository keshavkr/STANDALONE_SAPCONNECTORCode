package com.SAPUMEStandAlone;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.openspml.message.AddRequest;
import org.openspml.message.AddResponse;
import org.openspml.message.Attribute;
import org.openspml.message.DeleteRequest;
import org.openspml.message.FilterTerm;
import org.openspml.message.ModifyRequest;
import org.openspml.message.SchemaRequest;
import org.openspml.message.SearchRequest;
import org.openspml.message.SearchResponse;
import org.openspml.message.SearchResult;
import org.openspml.message.SpmlResponse;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAPUMEConnector {
	
	static SpmlResponse spmlResponse = null;
	
	public static List groupList = new ArrayList();
	public static List roleList = new ArrayList();
	static List<String> attrs = new ArrayList<String>();
	
	public static void main(String[] args) {
		
		String uname = "Keshav";
		// Add group which you want to assign to user
		//groupList.add("GRUP.PRIVATE_DATASOURCE.un:ADSCallers");
		// Add Roles which you want to assign to user
		//roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:CAFAdmin");
		//roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:CAFUIAdmin");
			// Creation of User
				createUser(uname);
			// Modify User
			//	modifyUser(uname);
			// Deletion of user
			//	deleteUser(uname);
			//	reconcileChidData();
			// searchUser(uname);	
			// String uid = getUniqueIdentifier(uname);
			// System.out.println("uid......"+uid);
		//attrs = getAtrributesFomTarget();
	}
	
	/**
	 * 
	 */
	
	public static List<String>  getAtrributesFomTarget(){
		List<String> attributes = new ArrayList<String>();
		try {			
			SchemaRequest schmaReq = new SchemaRequest();
			//schmaReq.setProviderIdentifier("SAP");
			schmaReq.setSchemaIdentifier("SAPprincipals");
			SpmlResponse spmlResponse = processRequest(schmaReq.toXml());
			
			attrs = getAttributes4mSpmlResponse(spmlResponse);
			   System.out.println("Number of attributes ...."+attrs.size());
			   Set<AttributeInfo> attrInfos = new HashSet<AttributeInfo>();
			   for (Iterator iterator = attrs.iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				System.out.println("attribute Names ::"+attr);
				/*//Schema building
				 AttributeInfoBuilder attrBuilder = new AttributeInfoBuilder();
		            attrBuilder.setName(attr);
		            attrBuilder.setCreateable(true);
		            attrBuilder.setUpdateable(true);
		            attrInfos.add(attrBuilder.build());	*/			
			   }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return attributes;
	}
	/**
	 * This method will provision user
	 * @param uname
	 */	
	
	public static void createUser(String uname) {
		// Creating SPML addRequest
		AddRequest addReq = new AddRequest();
		// Adding SPML attributes
			addReq.setObjectClass("sapuser");
			addReq.setAttribute("logonname", uname);
			addReq.setAttribute("firstname", uname);
			addReq.setAttribute("lastname", uname);
			addReq.setAttribute("password", "initial16");
			addReq.setAttribute("validto", "20201031000000Z");			
			
			// assigning group and role to the user
			//addReq.setAttribute("assignedgroups", groupList);
			//addReq.setAttribute("assignedroles", roleList);

		try{
			// process the Provisioning request
			SpmlResponse spmlResponse = processRequest(addReq.toXml());		
			String sUid = getResponse(spmlResponse, uname);
			System.out.println(sUid + " created succesfully"+spmlResponse.toXml());
		} catch (Exception eException) {
			System.out.println("Create User Failed : {0}"+ eException.getMessage());
		}
	}
	

	
	/**
	 * Method to modify attribute values
	 * @param uid
	 */
	
	public static void modifyUser(String uid){
		// Creating SPML modifyRequest
		ModifyRequest modifyReq = new ModifyRequest();
		// Mention attribute name and attribute value to be modified 
		String attrName = "logonname";
		String attrValue = "test222";
		
		modifyReq.addModification(attrName, attrValue);
		
        uid = "USER.PRIVATE_DATASOURCE.un:"+uid;
		modifyReq.setIdentifier(uid);
	
		SpmlResponse spmlResponse;
		try {
			spmlResponse = processRequest(modifyReq.toXml());
			String sUid = getResponse(spmlResponse, uid);
			System.out.println(sUid+"Modified Successfully");
		} catch (Exception e) {			
			e.printStackTrace();
		}	
	}
	
	/**
	 * This will delete accounts (User or Group) in target by creating
	 * DeleteRequest.
	 * 
	 * @param String
	 *            uid of the User/Group to be deleted
	 */
	public static void deleteUser(String uid) {
		// Creating SPML deleteRequest
		DeleteRequest delReq = new DeleteRequest();
		System.out.println("Deleting user :{0} "+ uid);
		// EX: uid =SAP.PRIVATEDATASOURCE.un:sapuser1
		uid = "USER.PRIVATE_DATASOURCE.un:"+uid;
		try {			
			delReq.setIdentifier(uid);
			SpmlResponse spmlResponse = processRequest(delReq.toXml());
			String sUid = getResponse(spmlResponse, uid);
			System.out.println("Deleted user:{0} Successfully  "+sUid);
		} catch (Exception eException) {
			System.out.println("Delete user failed : {0}"+ eException.getMessage());
		}
		
	}
	

	/**
	 * This method process the SPML Request and returns SPML response
	 * @param req
	 * @return
	 * @throws Exception 
	 */
	public static SpmlResponse processRequest(String req) throws Exception {		
			String sResponse = "";
 			SpmlResponse spmlResponse = null;
			String sSOAPRequest = formRequest(req);
			
			try {
				// Mention target details to connect
				//Connect(sURL,userName,Password)
				// sURL = "http://<server>:<port>/spml/spmlservice"
				// sUserName= <username>
				// sPassword = <password>
				
				HttpURLConnection objHttpURLConnection = connect(
						"http://10.30.32.171:50000/spml/spmlservice", "ZOIMUSER",
						"Oracle123");
				
				byte[] xmlBytes = sSOAPRequest.getBytes();
				objHttpURLConnection.setRequestProperty("Content-Length", String
						.valueOf(xmlBytes.length));
				objHttpURLConnection.setRequestProperty("Content-Type",
						"text/xml; charset=UTF-8");
				objHttpURLConnection.setRequestProperty("X-Requested-With",
				"XMLHttpRequest");
				objHttpURLConnection.setRequestProperty("SOAPAction", "POST");			
				objHttpURLConnection.setRequestMethod("POST");
				
				objHttpURLConnection.setDoOutput(true);
				objHttpURLConnection.setDoInput(true);

				OutputStream out = objHttpURLConnection.getOutputStream();
				out.write(xmlBytes);
				out.close();

				InputStreamReader isr = new InputStreamReader(objHttpURLConnection
						.getInputStream());
				BufferedReader in = new BufferedReader(isr);

				String inputLine;
				StringBuffer result = new StringBuffer();
				while ((inputLine = in.readLine()) != null)
					result.append(inputLine);
				in.close();
				sResponse = result.length() == 0 ? null : result.toString();
				spmlResponse = SpmlResponse.parseResponse(sResponse);
			}
			// wrong user name and password
			catch (ProtocolException pException) {
				throw new Exception(pException.getMessage());
			} catch (FileNotFoundException fException) {
				// when user name and password is wrong
				if (fException.getMessage().contains("401: Unauthorized"))
					throw new Exception(fException.getMessage());
				// when filename in the url is wrong
				else
					throw new Exception(fException);
			}
			// when given wrong port number
			catch (IllegalArgumentException iException) {
				throw new Exception(iException.getMessage());

			}
			// when host name is wrong
			catch (UnknownHostException iException) {
				throw new Exception(iException.getMessage());

			}
			// if IP address is wrong
			catch (ConnectException iException) {
				throw new Exception(iException.getMessage());

			} catch (IOException ioException) {
				throw new Exception(ioException.getMessage());

			} catch (Exception eException) {
				throw new Exception(eException.getMessage());

			}
			System.out.println("SPML Response....."+spmlResponse.toXml());
			return spmlResponse;
	}
	
	/**
	 * Determines SPML response status either success, failure,
	 * 
	 * @param SpmlResponse
	 *            spmlResponse
	 * @param String
	 *            uid
	 * 
	 * @return String user id
	 * @throws Exception 
	 */
	public static String getResponse(SpmlResponse spmlResponse, String uid) throws Exception {
		String sUID = null;

		// ///////////////////////////////
		// Handling Failure situations
		// ///////////////////////////////
		if (spmlResponse.getResult().contains("failure")) {
			// User doesn't exist in target
			if (spmlResponse.getErrorMessage().contains("doesn't exist")) {
				System.out.println("User doesn't exist in the target system ");
				throw new Exception(
						"Uid doesn't exist in the target system: {0} " + uid);
			}
			// If password doesn't meet target system standards
			else if (spmlResponse.getErrorMessage().contains(
					"ALPHANUM_REQUIRED_FOR_PSWD")
					|| spmlResponse.getErrorMessage().contains(
							"PASSWORD_TOO_SHORT")
					|| spmlResponse.getErrorMessage().contains(
							"PASSWORD_TOO_LONG")) {
				System.out.println("Invalid password exception");
				throw new Exception(spmlResponse
						.getErrorMessage());
			}
			// If user already exists in target
			else if (spmlResponse.getErrorMessage().contains("already exists")) {

				System.out.println("User Already exists in the target system");
				throw new Exception(
						"User Already exists in the target system");

			}
			// Other exceptions
			else {
				throw new Exception(spmlResponse.getErrorMessage());
			}
		}
		// Handling Success situations
		else if (spmlResponse.getResult().contains("success")) {
			if (spmlResponse instanceof AddResponse) {
				sUID = ((AddResponse) spmlResponse).getIdentifierString();
			} else {
				sUID = uid;
			}
		}
		return sUID;
	}
	
	/**
	 * Getting Connection to target
	 * @param sUrl
	 * @param sUsername
	 * @param sPassword
	 * @return
	 * @throws Exception
	 */

	public static HttpURLConnection connect(String sUrl,
			final String sUsername, final String sPassword) throws Exception {
		URLConnection httpConn = null;
		try {
			URL url = new URL(sUrl);
			URLConnection connection = url.openConnection();
			httpConn = (HttpURLConnection) connection;

			String connectStr = sUsername + ":" + sPassword;
			String encoding = new sun.misc.BASE64Encoder().encode(connectStr
					.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + encoding);

			if (sUsername != null && sPassword != null) {
				Authenticator.setDefault(new Authenticator() {
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(sUsername, sPassword
								.toCharArray());
					}
				});
			}
			
		} catch (Exception e) {
			System.out.println("Error - connect : " + e.getMessage());
		}
		return (HttpURLConnection) httpConn;
	}
	
	/**
	 * Forming spml request with SOAP header
	 * @param sReq
	 * @return
	 */
	
	public static String formRequest(String sReq) {
		StringBuffer sbCRUD = new StringBuffer();
		StringBuffer sbRequest = new StringBuffer();
		sbRequest
				.append("<?xml version='1.0' encoding='UTF-8'?> \n")
				.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"> \n")
				.append("<SOAP-ENV:Header X-Requested-With='XMLHttpRequest'/> \n")
				.append("<SOAP-ENV:Body> \n")
				.append(sReq)
				.append("</SOAP-ENV:Body> \n")
				.append("</SOAP-ENV:Envelope> \n");
		System.out.println("SPML request... "+sbRequest.toString());
		
		return sbRequest.toString();
	}
	
	/**
	 * Reconciliation of child data
	 */
	private static void reconcileChidData() {
		
	}
	/**
	 * Get the User credentials with SearchRequest
	 * @param uid
	 */
	private static void searchUser(String uid ) {
		
		SearchRequest req = new SearchRequest();
		
	   // specify the attributes to return
	   req.addAttribute("logonname");
	   req.addAttribute("password");
	   req.addAttribute("firstname");
	   req.addAttribute("lastname");
	   req.addAttribute("uniquename");
	   req.addAttribute("id");
	   req.addAttribute("datasource");
//	   req.addAttribute("assignedgroups");
//	   req.addAttribute("assignedroles");
	   
	  // uid = "USER.PRIVATE_DATASOURCE.un:"+uid;
	  // req.setIdentifier(uid);
	   req.setSearchBase("sapuser");
	   // specify the filter 
	   FilterTerm ft = new FilterTerm();
	   ft.setOperation(FilterTerm.OP_EQUAL);
	   ft.setName("logonname");
	   ft.setValue("JK1FEB20");
	   req.addFilterTerm(ft);
	   SearchResponse res;
	   Map user = null;
	try {
		res = (SearchResponse)processRequest(req.toXml());
		List results = res.getResults();
		/* if (results != null)  {
		      for (int i = 0 ; i < results.size() ; i++) {
		         SearchResult sr = (SearchResult)results.get(i);
		         System.out.println("Identifier=" +
		                              sr.getIdentifierString() +
		                              " logonname=" +
		                              sr.getAttributeValue("logonname")+
		                              " validto=" +
		                              sr.getAttributeValue("validto")+
		                              " password=" +		                              
		                              sr.getAttributeValue("password"));
		         }*/
		
  		System.out.println("11111111111111********: ");

		  	if (results != null) {
				System.out.println("results.size() ********: "+results.size());
			   SearchResult sr = (SearchResult)results.get(0);
			   user = new HashMap();
//			    Identifier id = sr.getIdentifier();
//			    user.put("accountId", id.getId());

			    List atts = sr.getAttributes();
			    if (atts != null) {
				Iterator it = atts.iterator();
					while (it.hasNext()) {
					    Attribute att = (Attribute)it.next();
					    user.put(att.getName(), att.getValue());
					    System.out.println("Attribute Name ********: "+att.getName());
					    System.out.println("Attribute Value ********: "+att.getValue());
					}
			    }
		   }
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   // display the results
	   
	}
	
	/**
	 * Get unique identifier using Search request
	 */
	
	public static String getUniqueIdentifier(String uid){
			String retUid = null;	
			SearchRequest searchReq = new SearchRequest();
		   // specify the attributes to return
			searchReq.addAttribute("id");
			searchReq.addAttribute("datasource");
		   
			searchReq.setSearchBase("sapuser");
		   // specify the filter 
		   FilterTerm ft = new FilterTerm();
		   ft.setOperation(FilterTerm.OP_EQUAL);
		   ft.setName("logonname");
		   ft.setValue(uid);
		   searchReq.addFilterTerm(ft);
		   SearchResponse res;
		try {
			SpmlResponse spmlResponse = processRequest(searchReq.toXml());
			SearchResponse resp = (SearchResponse) spmlResponse;
			List results = resp.getResults();
			  	if (results != null) {
				   SearchResult sr = (SearchResult)results.get(0);
				   retUid = sr.getIdentifierString();
				    List<Attribute> attrs = sr.getAttributes();
				    if (attrs != null) {
					Iterator<Attribute> it = attrs.iterator();
						while (it.hasNext()) {
						    Attribute atr = (Attribute)it.next();
						    System.out.println("Attribute Name ********: "+atr.getName());
						    System.out.println("Attribute Value ********: "+atr.getValue());
						    if(atr.getName().equals("id")){
						    	retUid = atr.getValue().toString();
						    }
						}
				    }
			   }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return retUid;		
	}
	
	
	   
	public static List<String> getAttributes4mSpmlResponse(SpmlResponse spmlResponse){
	  try {  
	   // obtain and configure a SAX based parser  
	   SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();  
	  
	   // obtain object for SAX parser  
	   javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
	   
	   // default handler for SAX handler class  
	   // all three methods are written in handler's body  
	   DefaultHandler defaultHandler = new DefaultHandler(){	      
	    String attributeDefinitionReference="close";	   
	      
	    // this method is called every time the parser gets an open tag '<'  
	    // identifies which tag is being open at time by assigning an open flag  
	    public void startElement(String uri, String localName, String qName,  
	      Attributes attributes) throws SAXException {	     
	     if (qName.equalsIgnoreCase("spml:attributeDefinitionReference")) {  
	      attributeDefinitionReference = "open";
	      String attrName = attributes.getValue("name");
	      attrs.add(attrName);
	      }  
	    }  
	   };  
	   saxParser.parse(new InputSource(new StringReader(spmlResponse.toXml())), defaultHandler);
	  } catch (Exception e) {  
	   e.printStackTrace();  
	  }
	    return attrs;
	 }
	
}
