package com.aceplus.myanmar_padauk.utils;

import java.util.ArrayList;

public class TLAJsonStringMaker
{
	public static String jsonStringMaker(ArrayList<String> keyList, ArrayList<String> valueList)
	{
		String resultJsonStr = "{";

		for(int i=0; i<keyList.size(); i++)
		{
			resultJsonStr += "\"" + keyList.get(i) + "\":\"" + valueList.get(i) + "\",";
		}

		resultJsonStr = resultJsonStr.substring(0, resultJsonStr.length() - 1);		// remove ,
		resultJsonStr += "}";

		return resultJsonStr;
	}

	public static String jsonArrayStringMaker(ArrayList<String> keyList, ArrayList<ArrayList<String>> valueList)
	{
		String resultJsonStr = "[";

		for(int j=0; j<valueList.size(); j++)
		{
			resultJsonStr += "{";
			
			ArrayList<String> customerInfo = valueList.get(j);
			
			for(int i=0; i<customerInfo.size(); i++)
			{
				resultJsonStr += "\"" + keyList.get(i) + "\":\"" + customerInfo.get(i) + "\",";
			}
			
			resultJsonStr = resultJsonStr.substring(0, resultJsonStr.length() - 1);		// remove ,
			
			resultJsonStr += "},";
		}

		resultJsonStr = resultJsonStr.substring(0, resultJsonStr.length() - 1);		// remove ,
		resultJsonStr += "]";

		return resultJsonStr;
	}
}
