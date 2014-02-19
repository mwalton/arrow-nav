package com.hci.cyclenav.util;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.hci.cyclenav.R;
import com.hci.cyclenav.guidance.GuidanceNode;
import com.hci.cyclenav.guidance.GuidanceNode.maneuver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NarrativeListAdapter extends ArrayAdapter<GuidanceNode> {
	
	private final Context context;
    private final ArrayList<GuidanceNode> nodeItems;

	public NarrativeListAdapter(Context context, ArrayList<GuidanceNode> nodes) {
		super(context, R.layout.row, nodes);
		this.context = context;
        this.nodeItems = nodes;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater 
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.row, parent, false);

        // 3. Get the two text view from the rowView
        ImageView imgView = (ImageView) rowView.findViewById(R.id.maneuverIcon);
        TextView labelView = (TextView) rowView.findViewById(R.id.label);
        TextView valueView = (TextView) rowView.findViewById(R.id.value);
        
        //set the maneuver icon
        imgView.setImageResource(getManeuverIcon(nodeItems.get(position)));

        // 4. Set the text for textView 
        
        labelView.setText(nodeItems.get(position).getInfo());
        
        double distIn_miles = nodeItems.get(position).getLength();
        
        //if the distance is smaller than ~1000 feet, measure in ft instead of miles
        if (distIn_miles > .17) {
        	String miles = new DecimalFormat("#.00").format(distIn_miles);
        	valueView.setText(miles + " miles");
        } else {
        	double distIn_feet = distIn_miles * GuidanceNode.FT_PER_MILE;
        	String feet = new DecimalFormat("#.00").format(distIn_feet);
        	valueView.setText(feet + " feet");
        }
        
        

        // 5. retrn rowView
        return rowView;
    }
	
	public int getManeuverIcon(GuidanceNode g) {
		maneuver m = g.getManeuverType();
		//straight arrow cases
		if (m == maneuver.STRAIGHT || m == maneuver.BECOMES || 
				m == maneuver.STAY_STRAIGHT || m == maneuver.MERGE_STRAIGHT) {
			return R.drawable.straight;
		}
		//left arrow cases
		else if (m == maneuver.SHARP_LEFT ||
				m == maneuver.STAY_LEFT || m == maneuver.LEFT) {
			return R.drawable.left;
		}
		//right arrow cases
		else if (m == maneuver.SHARP_RIGHT ||
				m == maneuver.STAY_RIGHT || m == maneuver.RIGHT) {
			return R.drawable.right;
		}
		//slight right cases
		else if (m == maneuver.SLIGHT_RIGHT) {
			return R.drawable.slight_right;
		}
		//slight left cases
		else if (m == maneuver.SLIGHT_LEFT) {
			return R.drawable.slight_left;
		}
		//destination cases
		else if (m == maneuver.DESTINATION || m == maneuver.DESTINATION_LEFT ||
				m == maneuver.DESTINATION_RIGHT) {
			return R.drawable.flag;
		}
		//default case
		else {
			return R.drawable.usr_loc;
		}
	}

}
