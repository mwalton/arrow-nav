package com.hci.cyclenav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hci.cyclenav.guidance.GuidanceData;
import com.hci.cyclenav.guidance.GuidanceRoute;
import com.hci.cyclenav.guidance.HttpUtil;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class GuidanceNarrative extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guidance_narrative);
		
		Intent intent = getIntent();
		String destination = intent.getStringExtra(MainActivity.DESTINATION);
		double usrLat = intent.getDoubleExtra(MainActivity.USR_LAT, 0);
		double usrLng = intent.getDoubleExtra(MainActivity.USR_LNG, 0);
		
		TextView textView = (TextView) findViewById(R.id.test_narrative);
		textView.setMovementMethod(new ScrollingMovementMethod());
	    textView.setText(destination + "\n" + usrLat + "\n" + usrLng);
	    
	    //Construct the appropriate HTTP request and send it to JSONhelper
	    Toast.makeText(getApplicationContext(), "Calculating Route" , Toast.LENGTH_LONG).show();
	    String source = usrLat + ", " + usrLng;
	    
	    String request = new HttpUtil(this, source, destination).getHttp();
	    
	    Logger.getLogger(Logger.class.getName()).log(Level.INFO, request);
	    JSONHelper json = new JSONHelper();
    	json.execute(request);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.guidance_narrative, menu);
		return true;
	}
	
	class JSONHelper extends AsyncTask<String, String, String> {

	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        
	        //HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 3000);
	        //HttpConnectionParams.setSoTimeout(httpclient.getParams(), 10000);
	        
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException ex) {
	        	Logger.getLogger(Logger.class.getName()).log(Level.SEVERE,ex.getMessage());
	        } catch (IOException ex) {
	        	Logger.getLogger(Logger.class.getName()).log(Level.SEVERE,ex.getMessage());
	        }
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	TextView textView = (TextView) findViewById(R.id.test_narrative);
	    	
	    	JsonParser parser = new JsonParser();
	    	
	    	try {
	    		JsonObject obj = parser.parse(result).getAsJsonObject();
	    		Gson gson = new Gson();
	    		GuidanceData data = gson.fromJson(obj.get("guidance"), GuidanceData.class);
	    		GuidanceRoute route = new GuidanceRoute(data);
	    		textView.setText(route.toString());
	    	} catch (JsonSyntaxException er) {
				Toast.makeText(getApplicationContext(), er.toString(), Toast.LENGTH_LONG).show();
				textView.setText("\n\n\n\nBad JSON formatting: " + "\n" + er.toString());
	    	}
	    }
	}

}
