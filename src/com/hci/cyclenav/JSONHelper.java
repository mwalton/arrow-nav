package com.hci.cyclenav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;

public class JSONHelper {
	private URL url;
	private int timeout;
	private String data;

	public JSONHelper(String u, int t) {
		timeout = t;
		
		try {
			url = new URL(u);
		} catch (MalformedURLException ex) {
	        Logger.getLogger(Logger.class.getName()).log(Level.SEVERE,ex.getMessage());
	    }
	}
	
	public GuidanceRoute getGuidanceRoute() {
		Gson gson  = new Gson();
		return gson.fromJson(data, GuidanceRoute.class);
	}
	
	public void setUrl(String u) {
		try {
			url = new URL(u);
		} catch (MalformedURLException ex) {
	        Logger.getLogger(Logger.class.getName()).log(Level.SEVERE,ex.getMessage());
	    }
	}
	
	public void setTimeout(int t) {
		timeout = t;
	}
	
	public void getJSON() {
	    try {
	        HttpURLConnection c = (HttpURLConnection) url.openConnection();
	        c.setRequestMethod("GET");
	        c.setRequestProperty("Content-length", "0");
	        c.setUseCaches(false);
	        c.setAllowUserInteraction(false);
	        c.setConnectTimeout(timeout);
	        c.setReadTimeout(timeout);
	        c.connect();
	        int status = c.getResponseCode();

	        switch (status) {
	            case 200:
	            case 201:
	                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
	                StringBuilder sb = new StringBuilder();
	                String line;
	                while ((line = br.readLine()) != null) {
	                    sb.append(line+"\n");
	                }
	                br.close();
	                data = sb.toString();
	        } 
	    } catch (IOException ex) {
	    	Logger.getLogger(Logger.class.getName()).log(Level.SEVERE,ex.getMessage());
	    }
	    data = null;
	}
}
