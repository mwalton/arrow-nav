package com.hci.cyclenav;

import java.util.LinkedList;

import com.mapquest.android.maps.BoundingBox;
import com.mapquest.android.maps.GeoPoint;

public class GuidanceRoute {
	BoundingBox boundingBox;
	LinkedList<GuidanceNode> nodes;
	int currentNodeIndex;
	
	public GuidanceRoute() {
		// TODO Auto-generated constructor stub
	}
	
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    Takes raw GSON-generated guidance object and 	            :*/
	/*::    adapts it for use by our navigation system	 	            :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	
	public GuidanceRoute(GuidanceData d) {
		GeoPoint ul = new GeoPoint(d.boundingBox.ul.lat, d.boundingBox.ul.lng);
		GeoPoint lr = new GeoPoint(d.boundingBox.lr.lat, d.boundingBox.lr.lng);
		boundingBox = new BoundingBox(ul, lr);
		
		LinkedList<GeoPoint> pointList = getPoints(d.shapePoints);
		
		nodes = new LinkedList<GuidanceNode>();
		for (int i = 0; i < d.GuidanceNodeCollection.length; ++i) {
			//if the node is a turn, append it to the list
			if (d.GuidanceNodeCollection[i].infoCollection != null) {
				String info = d.GuidanceNodeCollection[i].infoCollection[0];
				
				int linkIndex = d.GuidanceNodeCollection[i].linkIds[0];
				int locIndex = d.GuidanceLinkCollection[linkIndex].shapeIndex;
				GeoPoint loc = pointList.get(locIndex);
				
				int maneuver = d.GuidanceNodeCollection[i].maneuverType;
				double length = d.GuidanceLinkCollection[linkIndex].length;
			
				nodes.add(new GuidanceNode(info, loc, maneuver, length));
			} else {
				/* if the node is not a turn, add the length of the
				*  corresponding link to the length of the link of the
				*  last node in the list
				*/
				if (d.GuidanceNodeCollection[i].linkIds.length > 0 &&
						nodes.size() > 0) {
					int linkIndex = d.GuidanceNodeCollection[i].linkIds[0];
					nodes.getLast().length += d.GuidanceLinkCollection[linkIndex].length;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(boundingBox.toString() + "\n");
		
		for (GuidanceNode g : nodes) {
			sb.append(g.toString());
		}
		return sb.toString();
	}
	
	//Adapter function converts the shapePoints array of length n
	// to a linkedList of latitude/longitude ordered pairs of size n / 2
	LinkedList<GeoPoint> getPoints(double[] latLngArray) {
		LinkedList<GeoPoint> pointList = new LinkedList<GeoPoint>();
		
		for(int i = 0; i < latLngArray.length; i += 2) {
			double lat = latLngArray[i];
			double lng = latLngArray[i + 1];
			
			pointList.add(new GeoPoint(lat, lng));
		}
		return pointList;
	}
	
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    Convenience functions for computing distance	            :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	
	//get the distance in miles between the user and the next node
	double milesToNextNode(GeoPoint usrLoc) {
		GeoPoint nextNode = nodes.get(currentNodeIndex + 1).location;
		return distance(usrLoc, nextNode);
	}
	
	//get the 'progress' or percent complete (a value between 0 - 1)
	//that represents how far the user has travelled along the current link
	double currentLinkProgress(GeoPoint usrLoc) {
		double dist = milesToNextNode(usrLoc);
		double linkLength = nodes.get(currentNodeIndex).length;
		double progress =  (linkLength - dist) / linkLength;
		
		return progress;
	}
	
	//get the distance in miles between two GeoPoints
	double distance(GeoPoint pnt1, GeoPoint pnt2) {
		double lat1 = pnt1.getLatitude();
		double lng1 = pnt1.getLongitude();
		double lat2 = pnt2.getLatitude();
		double lng2 = pnt2.getLongitude();
	
		double theta = lng1 - lng2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
	      
		return dist;
	}
	

    // This function converts decimal degrees to radians
    private double deg2rad(double deg) {
    	return (deg * Math.PI / 180.0);
    }

    //This function converts radians to decimal degrees
    private double rad2deg(double rad) {
    	return (rad * 180.0 / Math.PI);
    }
	
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*:: 	 Stores a single node (eg an intersection)	                :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    
	class GuidanceNode {
		String info;			//from guidanceNode
		GeoPoint location;		//from shapePoints[] via guidanceLink
		int maneuverType;		//from guidanceNode
		double length;
		int[] linkIds;			//from guidanceNode
		
		GuidanceNode(String info, GeoPoint point, int maneuverType, double length) {
			this.info = info;
			this.location = point;
			this.maneuverType = maneuverType;
			this.length = length;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(info + ":\n");
			sb.append("Length: " + length + "m\n");
			sb.append(location.toString() + "\n\n");
			
			return sb.toString();
		}
	}
}
