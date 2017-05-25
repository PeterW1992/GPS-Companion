package dissertation.GPSCompanionApp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import dissertation.GPSCompanionApp.R;
import dissertation.GPSCompanionApp.helpers.Journey;
import dissertation.GPSCompanionApp.helpers.DatabaseHandler;
import dissertation.GPSCompanionApp.helpers.Utils;


public class ExpandListViewSingleChildJourneys extends BaseExpandableListAdapter {
    private Context _context;
    private List<Journey> _listData; //Header text is the string key, List<String> is the value data and is a list of all child data
    private long fastestJourney;


    public ExpandListViewSingleChildJourneys(Context context, List<Journey> listData, long fastestJourney){
        this._context = context;
        this._listData = listData;
        this.fastestJourney = fastestJourney;
    }

    @Override
    public int getGroupCount() {
        return _listData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return _listData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return _listData.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Journey journey = (Journey) getGroup(groupPosition);

        long diff = journey.getDuration();

        String dur = Utils.getDurationFormat(diff);

        String headerTitle = Utils.getDateReadable(journey.getStartDateTime().substring(0, journey.getStartDateTime().indexOf("T"))) + " - " +  dur;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);

        lblListHeader.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_view_item_journey, null);
        }

        Journey journey = _listData.get(groupPosition);

        DatabaseHandler databaseHandler = new DatabaseHandler(_context);

        String maxSpeed = Utils.mpsToMph(databaseHandler.getAggForJourney("MAX", "Speed", journey.getRowid()));
        String avgSpeed = Utils.mpsToMph(databaseHandler.getAggForJourney("AVG", "Speed", journey.getRowid()));

        String maxAlt = Utils.formatValue(databaseHandler.getAggForJourney("MAX", "Alt", journey.getRowid()));
        String avgAlt = Utils.formatValue(databaseHandler.getAggForJourney("AVG", "Alt", journey.getRowid()));

        long fastestDiff = journey.getDuration() - fastestJourney;

        TextView lblStartTime = (TextView) convertView.findViewById(R.id.lbl_startDateTimeValue);
        TextView lblEndTime = (TextView) convertView.findViewById(R.id.lbl_endDateTimeValue);

        TextView lblSpeedValues = (TextView) convertView.findViewById(R.id.lbl_speedStatsValue);
        TextView lblAltValues = (TextView) convertView.findViewById(R.id.lbl_altitudeValuesValue);
        TextView lblDiffValues = (TextView) convertView.findViewById(R.id.lbl_timeDiffValuesValue);

        lblStartTime.setText(Utils.getTimeReadable(journey.getStartDateTime()));
        lblEndTime.setText(Utils.getTimeReadable(journey.getEndDateTime()));
        lblSpeedValues.setText("Max: " + maxSpeed + ", Avg: " + avgSpeed);
        lblAltValues.setText("Max: " + maxAlt + ", Avg: " + avgAlt);
        lblDiffValues.setText(Utils.getDurationFormat(fastestDiff));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
