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
import com.hci.cyclenav.util.NarrativeListAdapter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/* GuidanceNarrative.java
 * Layout: cycle-nav/res/layout/activity_guidance_narrative.xml
 * 
 * This activity queries the Open Guidance Service API with the
 * parameters set in the MainActivity.  The resulting JSON is deserialized
 * using Gson into GuidanceData which is a temporary class that mirrors
 * the OGS JSON structure.  This data is passed to a GuidanceRoute constructor
 * which extracts the relavant data and stores turn, distance and link (path)
 * information as a ArrayList of GuidanceNodes.
 * 
 * guidance classes are designed to have minimal network overhead in that
 * the API query need only be made once and then the necissary data is retained
 * for the rest of the navigation process.
 * 
 * This is achieved by implementing nodes as an ArrayList<GuidanceNode implements Parcelable>
 * more information can be found in GuidanceNode.java
 */

public class GuidanceNarrative extends Activity {
	public final static String GUIDANCE_NODES = "com.hci.cyclenav.GUIDANCE_NODES";
	private GuidanceRoute route;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guidance_narrative);
		
		//get the intent package that was passed by ActivityMain
		//and set the user location and destination to be sent to OGS
		Intent intent = getIntent();
		String destination = intent.getStringExtra(MainActivity.DESTINATION);
		double usrLat = intent.getDoubleExtra(MainActivity.USR_LAT, 0);
		double usrLng = intent.getDoubleExtra(MainActivity.USR_LNG, 0);
		
		route = new GuidanceRoute();
	    
	    //Construct the appropriate HTTP string for JSONhelper
	    Toast.makeText(getApplicationContext(), "Calculating Route" , Toast.LENGTH_LONG).show();
	    String source = usrLat + ", " + usrLng;
	    
	    String request = new HttpUtil(this, source, destination).getHttp();
	    
	    //json debuggin: check logcat for the exact http request used
	    Logger.getLogger(Logger.class.getName()).log(Level.INFO, request);
	    
	    //make the query and wait for a response in a asynchronous thread
	    JSONHelper json = new JSONHelper();
    	json.execute(request);
	}
	
	//begins the arrow UI turn-by-turn navigation
	public void beginNavigation(View view) {
    	Intent intent = new Intent(this, ArrowNavigation.class);
    	
    	//Serialize the GuidanceNodes and add them to the intent
    	intent.putParcelableArrayListExtra(GUIDANCE_NODES, route.getNodes());
    	
    	startActivity(intent);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.guidance_narrative, menu);
		return true;
	}
	
	//Makes the HTTP request and listens for the JSON response in a non-UI thread
	class JSONHelper extends AsyncTask<String, String, String> {

	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        
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
	    
	    //called when a response is received from the API
	    @Override
	    protected void onPostExecute(String result) {
	    	ListView listView = (ListView) findViewById(R.id.listview);
	    	
	    	JsonParser parser = new JsonParser();
	    	
	    	try {
	    		/* there are several JSON objects stored in the response
	    		*  we only want the "guidance" one which contains navigation
	    		*  so we use the normal JsonObject class to parse the data
	    		*  then get just the "guidance" object to generate GuidanceData */
	    		JsonObject obj = parser.parse(result).getAsJsonObject();
	    		Gson gson = new Gson();
	    		GuidanceData data = gson.fromJson(obj.get("guidance"), GuidanceData.class);
	    		route = new GuidanceRoute(data);
	    		// pass context and data to the custom adapter
		        NarrativeListAdapter adapter = new NarrativeListAdapter(getApplicationContext(), route.getNodes());
		        
				// setListAdapter
		        listView.setAdapter(adapter);
	    	} catch (JsonSyntaxException er) {
				Toast.makeText(getApplicationContext(), er.toString(), Toast.LENGTH_LONG).show();
	    	}
	    }
	}

}
