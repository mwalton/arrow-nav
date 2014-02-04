package com.hci.cyclenav;

import android.content.Context;

/* HttpUtil.java
 * 
 * Simple HTTP request helper class
 * Simplifies & sanitizes adding key-value pairs to be sent in the API call
 * This class is designed for use by JSONHelper
 * 
 */

public class HttpUtil {
	StringBuilder httpStr;
	String baseUrl;
	String to, from, api_key;
	
	int generalizeAfter, generalize, direction, avoidManeuverDuration;

	public HttpUtil(Context c, String src, String dest) {
		httpStr = new StringBuilder();
		baseUrl = "http://open.mapquestapi.com/guidance/v1/route?";
		
		to = dest;
		from = src;
		api_key = c.getString(R.string.api_key);
		
		httpStr.append(baseUrl);	//start w/ the URL
		append("key", api_key);		//append the API key
	}
	
	//Builds and returns the finished HTTP request
	public String getHttp() {
		//if any keys are undefined, set them to default values
		if(httpStr.indexOf("routeType") < 0) append("routeType", "bicycle");
		if(httpStr.indexOf("narrativeType") < 0) append("narrativeType", "text");
		if(httpStr.indexOf("shapeFormat") < 0) append("shapeFormat", "raw");
		if(httpStr.indexOf("outFormat") < 0) append("outFormat", "json");
		if(httpStr.indexOf("units") < 0) append("units", 'm');
		if(httpStr.indexOf("fishbone") < 0) append("fishbone", "false");
		if(httpStr.indexOf("generalizeAfter") < 0) append("generalizeAfter", 500);
		if(httpStr.indexOf("generalize") < 0) append("generalize", 0);
		if(httpStr.indexOf("direction") < 0) append("direction", -1);
		if(httpStr.indexOf("avoidManeuverDuration") < 0) append("avoidManeuverDuration", -1);
		
		//return the finished HTTP request string
		return httpStr.toString();
	}
	
	//Append methods
	public void append(String key, String value) {
		String tmp = key + "=" + value;
		if (httpStr.length() > 1) httpStr.append('&');
		httpStr.append(tmp);
	}
	
	public void append(String key, int value) {
		String tmp = key + "=" + value;
		if (httpStr.length() > 1) httpStr.append('&');
		httpStr.append(tmp);
	}
	
	public void append(String key, char value) {
		String tmp = key + "=" + value;
		if (httpStr.length() > 1) httpStr.append('&');
		httpStr.append(tmp);
	}
}
