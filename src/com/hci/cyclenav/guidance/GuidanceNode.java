package com.hci.cyclenav.guidance;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapquest.android.maps.GeoPoint;

/* GuidanceNode.java
 * Android cannot pass objects directly from one activity to another
 * We make the OGS query in GuidanceNarrative and display the data
 * for user confirmation.  However, we obviously need the same data
 * to be avalable during navigation.  Therefore we use android parceling
 * to serialize the routeNodes into string arrays and pass them to the
 * ArrowNavigation activity in an intent.
 * 
 * Parceling works like normal java serializaiton except we are fully
 * explicit about which variables get stored where.  This takes a little
 * more work to implement (because we need to manually define the
 * writeToParcel and a constructor that takes a parcel param) but
 * parceling operates roughly 10x faster and requires less garbage collection
 * in the process.  This efficiancy is especially valuable in this context 
 * because the user must wait while the initial API call is made and the 
 * JSON is parsed after the MainActivity, it would be annoying to hang 
 * again between the narrative and arrownav screens.
 */

public class GuidanceNode implements Parcelable {
	public static final int FT_PER_MILE = 5280; // for m <-> ft conversion

	// maneuvers that occur at nodes/transitions between links
	public enum maneuver {
		NONE, // 0 No maneuver occurs here.
		STRAIGHT, // 1 Continue straight.
		BECOMES, // 2 No maneuver occurs here. Road name changes.
		SLIGHT_LEFT, // 3 Make a slight left.
		LEFT, // 4 Turn left.
		SHARP_LEFT, // 5 Make a sharp left.
		SLIGHT_RIGHT, // 6 Make a slight right.
		RIGHT, // 7 Turn right.
		SHARP_RIGHT, // 8 Make a sharp right.
		STAY_LEFT, // 9 Stay left.
		STAY_RIGHT, // 10 Stay right.
		STAY_STRAIGHT, // 11 Stay straight.
		UTURN, // 12 Make a U-turn.
		UTURN_LEFT, // 13 Make a left U-turn.
		UTURN_RIGHT, // 14 Make a right U-turn.
		EXIT_LEFT, // 15 Exit left.
		EXIT_RIGHT, // 16 Exit right.
		RAMP_LEFT, // 17 Take the ramp on the left.
		RAMP_RIGHT, // 18 Take the ramp on the right.
		RAMP_STRAIGHT, // 19 Take the ramp straight ahead.
		MERGE_LEFT, // 20 Merge left.
		MERGE_RIGHT, // 21 Merge right.
		MERGE_STRAIGHT, // 22 Merge.
		ENTERING, // 23 Enter state/province.
		DESTINATION, // 24 Arrive at your destination.
		DESTINATION_LEFT, // 25 Arrive at your destination on the left.
		DESTINATION_RIGHT, // 26 Arrive at your destination on the right.
		ROUNDABOUT1, // 27 Enter the roundabout and take the 1st exit.
		ROUNDABOUT2, // 28 Enter the roundabout and take the 2nd exit.
		ROUNDABOUT3, // 29 Enter the roundabout and take the 3rd exit.
		ROUNDABOUT4, // 30 Enter the roundabout and take the 4th exit.
		ROUNDABOUT5, // 31 Enter the roundabout and take the 5th exit.
		ROUNDABOUT6, // 32 Enter the roundabout and take the 6th exit.
		ROUNDABOUT7, // 33 Enter the roundabout and take the 7th exit.
		ROUNDABOUT8, // 34 Enter the roundabout and take the 8th exit.
		TRANSIT_TAKE, // 35 Take a public transit bus or rail line.
		TRANSIT_TRANSFER, // 36 Transfer to a public transit bus or rail line.
		TRANSIT_ENTER, // 37 Enter a public transit bus or rail station
		TRANSIT_EXIT, // 38 Exit a public transit bus or rail station
		TRANSIT_REMAIN_ON // 39 Remain on the current bus/rail car
	}

	String info; // from guidanceNode
	GeoPoint location; // from shapePoints[] via guidanceLink
	maneuver maneuverType; // from guidanceNode
	double length;
	int linkId; // from guidanceNode

	GuidanceNode(String info, GeoPoint point, int maneuverType, double length,
			int linkId) {
		this.info = info;
		this.location = point;
		// "cast" from int to the corresponding maneuver enum
		this.maneuverType = maneuver.values()[maneuverType];
		this.length = length;
		this.linkId = linkId;
	}

	public String getInfo() {
		return info;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public maneuver getManeuverType() {
		return maneuverType;
	}

	public double getLength() {
		return length;
	}

	public int getLinkId() {
		return linkId;
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: PARCELING METHODS : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */

	// Deserialization constructor
	// read serialized fields from parcel and make a new GuidanceNode
	public GuidanceNode(Parcel in) {
		String[] data = new String[6];
		in.readStringArray(data);

		this.info = data[0];

		// Geopoint data is the only trick because it requires
		// two elements to make the location object
		double lat = Double.parseDouble(data[1]);
		double lng = Double.parseDouble(data[2]);
		this.location = new GeoPoint(lat, lng);

		this.maneuverType = maneuver.valueOf(data[3]);
		this.length = Double.parseDouble(data[4]);
		this.linkId = Integer.parseInt(data[5]);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	// Serializer
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// make a string array of all member variables
		dest.writeStringArray(new String[] { this.info,
				Double.toString(this.location.getLatitude()),
				Double.toString(this.location.getLongitude()),
				this.maneuverType.name(), Double.toString(this.length),
				Integer.toString(this.linkId) });
	}

	// Creator calls serialize/deserialize methods of this class
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public GuidanceNode createFromParcel(Parcel in) {
			return new GuidanceNode(in);
		}

		public GuidanceNode[] newArray(int size) {
			return new GuidanceNode[size];
		}
	};

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{info=" + info + "; ");
		sb.append("maneuver=" + maneuverType.name() + "; ");
		sb.append("Length=" + length + "; ");
		sb.append("location=(" + location.toString() + "); ");
		sb.append("linkId=" + linkId + "}");

		return sb.toString();
	}
}
