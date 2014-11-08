package com.ucsbeci.app;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private int ParentClickStatus=-1;
    private int ChildClickStatus=-1;
    private Context context;
    private ExpandableListActivity activity;
    private LayoutInflater inflater;
    private ArrayList<ECIobj> parents;
    private int lastExpandedGroupPosition;

    public MyExpandableListAdapter(ExpandableListActivity activity, Context context, ArrayList<ECIobj> list)
    {
        this.activity = activity;
        this.context = context;
        this.parents = list;
        // Create Layout Inflator
        inflater = LayoutInflater.from(context);
    }


    // This Function used to inflate parent rows view
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parentView)
    {
        if(activity instanceof CheckActivity) {
            final Check parent = (Check)parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow_check, parentView, false);

            // Get grouprow.xml file elements and set values
            ((TextView)convertView.findViewById(R.id.checkText1)).setText("Notes: " + parent.getNotes());
            ((TextView)convertView.findViewById(R.id.checkText2)).setText("Tag Id: " + parent.getTag_id());

            ImageView image = (ImageView)convertView.findViewById(R.id.stat_icon);
            if(parent.getStatus().equals("good")) image.setBackgroundResource(R.drawable.box_green);
            else if(parent.getStatus().equals("warning")) image.setBackgroundResource(R.drawable.box_orange);
            else image.setBackgroundResource(R.drawable.box_red);

            return convertView;
        } else if (activity instanceof TagActivity) {
            final MyTag parent = (MyTag)parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow_tag, parentView, false);

            // Get grouprow.xml file elements and set values
            ((TextView) convertView.findViewById(R.id.tagText1)).setText("Description: " + parent.getDescription());
            ((TextView) convertView.findViewById(R.id.tagText2)).setText("Location: " + parent.getLocation());

            ImageView image = (ImageView) convertView.findViewById(R.id.stat_icon);
            image.setImageResource(R.drawable.box_gray);

            return convertView;
        } else if (activity instanceof DeviceActivity) {
            final Device parent = (Device)parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow_device, parentView, false);

            // Get grouprow.xml file elements and set values
            ((TextView)convertView.findViewById(R.id.deviceText1)).setText("Type: " + parent.getType());
            ((TextView)convertView.findViewById(R.id.deviceText2)).setText("Id #: " + parent.getId());

            ImageView image = (ImageView)convertView.findViewById(R.id.stat_icon);
            image.setImageResource(R.drawable.box_blue);

            return convertView;
        } else if (activity instanceof LocActivity) {
            final Loc parent = (Loc)parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow_device, parentView, false);

            // Get grouprow.xml file elements and set values
            ((TextView)convertView.findViewById(R.id.deviceText1)).setText("Location: " + parent.getLocation());
            ((TextView)convertView.findViewById(R.id.deviceText2)).setText("Id #: " + parent.getId());

            ImageView image = (ImageView)convertView.findViewById(R.id.stat_icon);
            image.setImageResource(R.drawable.box_gray);

            return convertView;
        } else {
            return null;
        }
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parentView) {
        if(activity instanceof CheckActivity) {
            final Check check = (Check)parents.get(groupPosition);

            final NameValuePair child = check.getData().get(childPosition);
            convertView = inflater.inflate(R.layout.childrow_check, parentView, false);

            ((TextView) convertView.findViewById(R.id.checkText1)).setText(child.getName() + child.getValue());

            return convertView;
        } else if (activity instanceof TagActivity) {
            final MyTag tag = (MyTag)parents.get(groupPosition);
            final NameValuePair child = tag.getData().get(childPosition);
            convertView = inflater.inflate(R.layout.childrow_tag, parentView, false);

            ((TextView) convertView.findViewById(R.id.tagText1)).setText(child.getName() + child.getValue());

            return convertView;
        } else if (activity instanceof DeviceActivity) {
            final Device device = (Device)parents.get(groupPosition);
            final NameValuePair child = device.getData().get(childPosition);
            convertView = inflater.inflate(R.layout.childrow_device, parentView, false);

            ((TextView) convertView.findViewById(R.id.deviceText1)).setText(child.getName() + child.getValue());

            return convertView;
        } else if (activity instanceof LocActivity) {
            final Loc loc = (Loc)parents.get(groupPosition);
            final NameValuePair child = loc.getData().get(childPosition);
            convertView = inflater.inflate(R.layout.childrow_device, parentView, false);

            ((TextView) convertView.findViewById(R.id.deviceText1)).setText(child.getName() + child.getValue());

            return convertView;
        } else {
            return null;
        }
    }

    @Override
    public int getGroupCount() {
        return parents.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return parents.get(groupPosition).getData().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parents.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return parents.get(groupPosition).getData().get(childPosition);
    }

    // Call when parent row is clicked
    @Override
    public long getGroupId(int groupPosition) {

        ParentClickStatus = groupPosition;
        if(ParentClickStatus == 0) {
            ParentClickStatus = -1;
        }
        return groupPosition;
    }

    // Call when child row is clicked
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        if (ChildClickStatus != childPosition) {
            ChildClickStatus = childPosition;
        }
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEmpty() {
        return ((parents == null) || parents.isEmpty());
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        if(groupPosition != lastExpandedGroupPosition) {
            activity.getExpandableListView().collapseGroup(lastExpandedGroupPosition);
        }
        super.onGroupExpanded(groupPosition);
        lastExpandedGroupPosition = groupPosition;
    }
}
