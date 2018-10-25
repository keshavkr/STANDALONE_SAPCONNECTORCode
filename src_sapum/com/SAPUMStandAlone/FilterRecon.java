package com.SAPUMStandAlone;

import java.util.Date;
import java.util.HashMap;



import com.sap.conn.jco.JCoFunction;

public class FilterRecon {
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String sQuery = "First Name=AnanthuDec11";
		int iNoOfOR;
		int iNoOfAnd;
		String sKey;
		String sValue;
		// exp : regular expression variable for spliting the query
		// according to "|"
		String sArrORExp[] = sQuery.split("\\s[|]\\s");
		iNoOfOR = sArrORExp.length - 1;
		
		for (int i = 0; i <= iNoOfOR; i++) {
	
			// exp : regular expression variable for splitting the query
			// according
			// to "&"
			String sArrANDExp[] = sArrORExp[i].split("\\s[&]\\s");
			iNoOfAnd = sArrANDExp.length - 1;
			for (int j = 0; j <= iNoOfAnd; j++) {
				
				int iEquals = 0;
				// Get the key and value by checking first index of '='
				// Throw exception if query does not have '=' operator b/w
				// key and
				// value
				iEquals = sArrANDExp[j].indexOf('=');

				sKey = sArrANDExp[j].substring(0, iEquals).trim();
				sValue = sArrANDExp[j].substring(iEquals + 1).trim();
				
				System.out.println(sValue);

			}
			
		}
		}
	
	


}
