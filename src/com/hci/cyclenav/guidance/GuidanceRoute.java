package com.hci.cyclenav.guidance;

import java.util.ArrayList;

import com.mapquest.android.maps.GeoPoint;

/* GuidanceRoute.java
 * 
 * Container class for storing an ArrayList of GuidanceNodes
 * constructors accept either an arraylist of GuidanceNodes (for retrieval 
 * from a parcel) or GuidanceData for generating a new route from JSON
 * also has methods for calculating distance between nodes and the user
 */

public class GuidanceRoute {
	ArrayList<GuidanceNode> nodes;
	int currentNodeIndex;
	
	public GuidanceRoute() {
		// TODO Auto-generated constructor stub
	}
	
	//accept deserialized GuidanceNodes and wrap in a new route
	public GuidanceRoute(ArrayList<GuidanceNode> nodes) {
		currentNodeIndex = 0;
		this.nodes = nodes;
	}
	
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    Takes raw GSON-generated guidance object and 	            :*/
	/*::    adapts it for use by our navigation system	 	            :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	
	public GuidanceRoute(GuidanceData d) {
		ArrayList<GeoPoint> pointList = getPoints(d.shapePoints);
		
		nodes = new ArrayList<GuidanceNode>();
		currentNodeIndex = 0;
		
		for (int i = 0; i < d.GuidanceNodeCollection.length; ++i) {
			//if the node is a turn, append it to the list
			if (d.GuidanceNodeCollection[i].infoCollection != null) {
				String info = d.GuidanceNodeCollection[i].infoCollection[0];
				
				int linkIndex = d.GuidanceNodeCollection[i].linkIds[0];
				int locIndex = d.GuidanceLinkCollection[linkIndex].shapeIndex;
				GeoPoint loc = pointList.get(locIndex);
				
				int maneuver = d.GuidanceNodeCollection[i].maneuverType;
				double length = d.GuidanceLinkCollection[linkIndex].length;
			
				nodes.add(new GuidanceNode(info, loc, maneuver, length, linkIndex));
			} else {
				/* if the node is not a turn, add the length of the
				*  corresponding link to the length of the link of the
				*  last node in the list
				*/
				if (d.GuidanceNodeCollection[i].linkIds.length > 0 &&
						nodes.size() > 0) {
					int linkIndex = d.GuidanceNodeCollection[i].linkIds[0];
					nodes.get(nodes.size() - 1).length += d.GuidanceLinkCollection[linkIndex].length;
				}
			}
		}
	}
	
	//expose the nodes for serialization and parceling
	public ArrayList<GuidanceNode> getNodes() {
		return nodes;
	}
	
	public GuidanceNode getCurrent() {
		return nodes.get(currentNodeIndex);
	}
	
	public GuidanceNode getNext() {
		return nodes.get(currentNodeIndex + 1);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (GuidanceNode g : nodes) {
			sb.append(g.toString() + "\n\n");
		}
		return sb.toString();
	}
	
	//Adapter function converts the shapePoints array of length n
	// to a linkedList of latitude/longitude ordered pairs of size n / 2
	ArrayList<GeoPoint> getPoints(double[] latLngArray) {
		ArrayList<GeoPoint> pointList = new ArrayList<GeoPoint>();
		
		for(int i = 0; i < latLngArray.length; i += 2) {
			double lat = latLngArray[i];
			double lng = latLngArray[i + 1];
			
			pointList.add(new GeoPoint(lat, lng));
		}
		return pointList;
	}
}
