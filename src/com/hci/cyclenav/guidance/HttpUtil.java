package com.hci.cyclenav.guidance;

import com.hci.cyclenav.R;

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

		httpStr.append(baseUrl); // start w/ the URL
		append("key", api_key); // append the API key
		append("from", src);
		append("to", dest);
	}

	// Builds and returns the finished HTTP request
	public String getHttp() {
		// if any keys are undefined, set them to default values
		if (httpStr.indexOf("routeType") < 0)
			append("routeType", "bicycle");
		if (httpStr.indexOf("narrativeType") < 0)
			append("narrativeType", "text");
		if (httpStr.indexOf("fishbone") < 0)
			append("fishbone", "false");

		String noWhitespace = httpStr.toString().replaceAll("\\s+", "%20");

		// return the finished HTTP request string
		return noWhitespace;
	}

	// Append methods
	public void append(String key, String value) {
		String tmp = key + "=" + value;
		if (httpStr.length() > 1)
			httpStr.append('&');
		httpStr.append(tmp);
	}

	public void append(String key, int value) {
		String tmp = key + "=" + value;
		if (httpStr.length() > 1)
			httpStr.append('&');
		httpStr.append(tmp);
	}

	public void append(String key, char value) {
		String tmp = key + "=" + value;
		if (httpStr.length() > 1)
			httpStr.append('&');
		httpStr.append(tmp);
	}
}
