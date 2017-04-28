package dissertation.GPSCompanionApp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import dissertation.GPSCompanionApp.helpers.StayPointVisit;
import dissertation.GPSCompanionApp.R;
import dissertation.GPSCompanionApp.helpers.Utils;

import java.util.GregorianCalendar;
import java.util.List;


public class ExpandListViewSingleChildStayPointVisits extends BaseExpandableListAdapter {
    private Context _context;
    private List<StayPointVisit> _listData; //Header text is the string key, List<String> is the value data and is a list of all child data


    public ExpandListViewSingleChildStayPointVisits(Context context, List<StayPointVisit> listData){
        this._context = context;
        this._listData = listData;
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
        StayPointVisit visit = (StayPointVisit) getGroup(groupPosition);
        long diff = visit.getDuration();
        String dur = Utils.getDurationFormat(diff);

        String headerTitle = Utils.getDateReadable(visit.get_START().substring(0, visit.get_START().indexOf("T"))) + " - " +  dur;

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
            convertView = infalInflater.inflate(R.layout.list_view_item_stay_point_visit, null);
        }

        StayPointVisit visit = _listData.get(groupPosition);

        TextView lblStartTime = (TextView) convertView.findViewById(R.id.lbl_startDateTimeValue);
        TextView lblEndTime = (TextView) convertView.findViewById(R.id.lbl_endDateTimeValue);

        lblStartTime.setText(Utils.getTimeReadable(visit.get_START()));
        lblEndTime.setText(Utils.getTimeReadable(visit.get_END()));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
