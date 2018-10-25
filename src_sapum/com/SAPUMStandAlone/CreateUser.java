package com.SAPUMStandAlone;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.identityconnectors.framework.api.operations.GetApiOp;

import com.sap.conn.jco.JCo;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * basic examples for Java to ABAP communication  
 */
public class CreateUser
{
	static String query=null;
	static ArrayList<String> userIdList=new ArrayList<String>();
    static String DESTINATION_NAME1 = "ABAP_AS_WITHOUT_POOL";
    
    /**
     * This example demonstrates the destination concept introduced with JCO 3.
     * The application does not deal with single connections anymore. Instead
     * it works with logical destinations like ABAP_AS and ABAP_MS which separates
     * the application logic from technical configuration.     
     * @throws JCoException
     */
    public static void step1Connect() throws JCoException
    {
        JCoDestination destination = JCoDestinationManager.getDestination(DESTINATION_NAME1);
        System.out.println("Attributes:");
       // System.out.println(destination.getAttributes());
        System.out.println();
    }
    
    
    static
    {
        Properties connectProperties = new Properties();
        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.30.32.46");
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR,  "00");
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "001");
        connectProperties.setProperty(DestinationDataProvider.JCO_USER,   "OIMUSER");
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "oracle1234");
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG,   "en");
        createDestinationDataFile(DESTINATION_NAME1,connectProperties);
    }
        
    static void createDestinationDataFile(String destinationName, Properties connectProperties)
    {
        File destCfg = new File(destinationName+".jcoDestination");
        try
        {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "for tests only !");
            fos.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }
    
    static void createUser(){
    	try {
			JCoFunction function = null;
			JCoDestination destination = JCoDestinationManager.getDestination(DESTINATION_NAME1);
			JCoContext.begin(destination);
			
			JCoRepository mRepository = destination.getRepository();
			JCoFunctionTemplate createFunctionTemplate;
			createFunctionTemplate = mRepository.getFunctionTemplate("BAPI_USER_CREATE1");
			function = createFunctionTemplate.getFunction();
			JCoParameterList jcoParameterList = function.getImportParameterList();
			JCoStructure jcoStructure = null;
			
			jcoStructure = jcoParameterList.getStructure("ADDRESS");
			
			jcoParameterList.setValue("USERNAME", "Roony");
			jcoStructure.setValue("E_MAIL", "Roony@gmail.com");
			jcoStructure.setValue("FIRSTNAME", "Roony");
			jcoStructure.setValue("LASTNAME", "Roony");
			jcoStructure = jcoParameterList.getStructure("PASSWORD");
			jcoStructure.setValue("BAPIPWD", "Password");
			function.execute(destination);
			System.out.println("The user "+jcoParameterList.getValue("USERNAME")+" is created successfully");
		} catch (JCoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    	
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++
    static void getTableData(String sQuery){

		try {
			JCoDestination destination;
			destination = JCoDestinationManager.getDestination(DESTINATION_NAME1);
			JCoContext.begin(destination);			
	        JCoRepository sapRepository;
			String tableName = null;	
			HashMap<String,String> customQueryKeyValues = new HashMap<String,String>();
			
	       //get all the key values of custom queries in a hashmap
	        customQueryKeyValues = getCustomQueryKeyValues();
	        
	        //JCoContext.begin(destination);
            sapRepository = destination.getRepository();
            
            if (sapRepository == null) {
                System.out.println("Couldn't get repository!");
                System.exit(0);
            } 
            
            JCoFunctionTemplate template2 = sapRepository.getFunctionTemplate("RFC_READ_TABLE");
            System.out.println("Getting template");
            JCoFunction function2 = template2.getFunction();

            	// decides which table to use
            	if((sQuery.contains("First Name")) || (sQuery.contains("Accounting Number")) || (sQuery.contains("Last Name")) || (sQuery.contains("User Id"))){
            		
            		tableName = "USER_ADDRP";
            	}
            	else{
            		tableName = "ADCP";
            	}
            	//creating the final query string for SAP from query given from OIM
            	query = createQuery(sQuery, customQueryKeyValues);
            	System.out.println("Query :: "+query);

            function2.getImportParameterList().setValue("QUERY_TABLE", tableName);

            JCoTable returnOptions = function2.getTableParameterList().getTable("OPTIONS");
            returnOptions.appendRow();
            
            returnOptions.setValue("TEXT", query);
            
            function2.execute(destination);
            JCoTable jcoTabled = function2.getTableParameterList().getTable("DATA");

            int numRows = jcoTabled.getNumRows();
            jcoTabled.firstRow();
            
        	ArrayList<String> personnelNumberList=new ArrayList<String>();
        	
        	//Fetching userId/personnelNumber from the response jcotable "jcoTabled"
            for (int i = 0; i < numRows; i++) {
				
            	jcoTabled.setRow(i);
            	String message = jcoTabled.getString("WA");
            	String userIdFromTable = null;
            	String personnelNumber = null;
           	
            	if(tableName.equals("USER_ADDRP")){
            		userIdFromTable = message.substring(3, 15).trim().toString();
            		userIdList.add(userIdFromTable);
            		System.out.println("UserId :: "+userIdList);
            	}
            	else{
            		personnelNumber = message.substring(13, 23).trim().toString();
            		personnelNumberList.add(personnelNumber);
            		System.out.println("PersonnelNumber :: "+personnelNumberList);
            		query = null;
            		getTableData("Accounting Number="+personnelNumber);
            	}
				
			}
	
		} catch (JCoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*finally {
            JCoContext.end(destination);
        }*/
		
    	
    }
    
	  
    public static HashMap getCustomQueryKeyValues(){
    	
    	HashMap<String,String> customQuery = new HashMap<String,String>();
    	
    	customQuery.put("Accounting Number", "PERSNUMBER");
    	customQuery.put("Valid From", "DATE_FROM");
    	customQuery.put("Valid Through", "DATE_TO");
    	customQuery.put("Building", "BUILDING");
    	customQuery.put("Department", "DEPARTMENT");
    	customQuery.put("Floor", "FLOOR");
    	customQuery.put("Room Number", "ROOMNUMBER");
    	customQuery.put("Telephone Number", "TEL_NUMBER");
    	customQuery.put("Telephone Extension", "TEL_EXTENS");
    	customQuery.put("Fax Extension", "FAX_EXTENS");
    	customQuery.put("Fax Number", "FAX_BUMBER");
    	customQuery.put("First Name", "NAME_FIRST");
    	customQuery.put("Last Name", "NAME_LAST");
    	
    	
		return customQuery;
    	
    }
    
    public static String createQuery(String sQuery, HashMap<String, String> customQueryKeyValues){
    	
    	String inetialQuery = null;
    	//String sQuery="First Name=Colman & Last Name=Mustard";
    	
        for (Entry<String, String> m:customQueryKeyValues.entrySet()) {
			
        	String keyValue = m.getKey();
        	
        	if(sQuery.contains(keyValue))
        		sQuery = sQuery.replaceAll(keyValue,customQueryKeyValues.get(keyValue));
        	
        	if(sQuery.contains("="))
        		sQuery = sQuery.replaceAll("=", " EQ '");
        	
        	if(sQuery.contains("&"))
        		sQuery = sQuery.replaceAll(" & ", "' And ");
        	
        	if(sQuery.contains("|"))
        		sQuery = sQuery.replaceAll(" | ", "' Or ");
        	System.out.println(inetialQuery);

        }
    	
    	sQuery = sQuery.concat("'");
    	
		//return inetialQuery;
    	return sQuery;
    }
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    public static void main(String[] args) throws JCoException
    {
        step1Connect();
        //createUser();
        //getTableData("Telephone Number=99999 & Fax Extension=99999");
    }
}
