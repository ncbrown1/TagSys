package com.ucsbeci.app;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends ExpandableListActivity {

    private List<Device> parents = new ArrayList<Device>();
    private Context context;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        Resources res = getResources();
        Drawable line = res.getDrawable(R.drawable.line);

        getExpandableListView().setGroupIndicator(null);
        getExpandableListView().setDivider(line);
        getExpandableListView().setDividerHeight(2);
        getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (childPosition == parents.get(groupPosition).getData().size() - 2) {
                    editDialog(groupPosition);
                    return true;
                } else if (childPosition == parents.get(groupPosition).getData().size() - 1) {
                    deleteDialog(groupPosition);
                    return true;
                }
                return false;
            }
        });
        registerForContextMenu(getExpandableListView());

        new HttpGetTask().execute();
        loadHosts((ArrayList) parents);
    }

    private void editDialog(final int groupPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        final LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_postdevice, null);
        builder.setView(dialogView);

        final Device thisDevice = parents.get(groupPosition);
        EditText type1 = (EditText) dialogView.findViewById(R.id.type_input);
        type1.setText(thisDevice.getType());
        EditText chars1 = (EditText) dialogView.findViewById(R.id.characteristics_input);
        chars1.setText(thisDevice.getData().get(1).getValue());
        EditText freq1 = (EditText) dialogView.findViewById(R.id.freq_input);
        freq1.setText(thisDevice.getFrequency() + "");

        builder.setTitle("New Check-In Post");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(DialogInterface dialog, int i) {
                EditText type = (EditText) dialogView.findViewById(R.id.type_input);
                EditText chars = (EditText) dialogView.findViewById(R.id.characteristics_input);
                EditText freq = (EditText) dialogView.findViewById(R.id.freq_input);

                thisDevice.setFrequency(Integer.parseInt(freq.getText().toString()));
                thisDevice.setType(type.getText().toString());
                thisDevice.setCharacteristics(chars.getText().toString());

                new HttpPutTask().execute(thisDevice);
                new HttpGetTask().execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(context, "Cancelled Post Sequence", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    private void deleteDialog(final int groupPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!");
        builder.setMessage("Are you sure you want to delete this record?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new HttpDeleteTask().execute(parents.get(groupPosition));
                new HttpGetTask().execute();
                getExpandableListView().collapseGroup(groupPosition);
                Toast.makeText(context, "Record " + parents.get(groupPosition).getId() + " Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(context, "Deletion Aborted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void loadHosts(final ArrayList<Device> newParents)
    {
        if (newParents == null)
            return;

        parents = newParents;

        // Check for ExpandableListAdapter object
        if (this.getExpandableListAdapter() == null)
        {
            //Create ExpandableListAdapter Object
            final MyExpandableListAdapter mAdapter = new MyExpandableListAdapter();

            // Set Adapter to ExpandableList Adapter
            this.setListAdapter(mAdapter);
        }
        else
        {
            // Refresh ExpandableListView data
            ((MyExpandableListAdapter)getExpandableListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new HttpGetTask().execute();
            loadHosts((ArrayList)parents);
            return true;
        } else if (id == R.id.action_add_new) {
            Device device = new Device();
            dialogSequence(device);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialogSequence(final Device start) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog_postdevice, null);
        builder.setView(dialogView);
        builder.setTitle("New Device Post");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(DialogInterface dialog, int i) {
                EditText type = (EditText) dialogView.findViewById(R.id.type_input);
                EditText chars = (EditText) dialogView.findViewById(R.id.characteristics_input);
                EditText freq = (EditText) dialogView.findViewById(R.id.freq_input);

                start.setFrequency(Integer.parseInt(freq.getText().toString()));
                start.setType(type.getText().toString());
                start.setCharacteristics(chars.getText().toString());

                new HttpPostTask().execute(start);
                new HttpGetTask().execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(context, "Cancelled Post Sequence", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    private class MyExpandableListAdapter extends BaseExpandableListAdapter
    {
        private int ParentClickStatus=-1;
        private int ChildClickStatus=-1;
        private LayoutInflater inflater;
        private int lastExpandedGroupPosition;

        public MyExpandableListAdapter()
        {
            // Create Layout Inflator
            inflater = LayoutInflater.from(DeviceActivity.this);
        }


        // This Function used to inflate parent rows view

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parentView)
        {
            final Device parent = parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow_device, parentView, false);

            // Get grouprow.xml file elements and set values
            assert convertView != null;
            ((TextView)convertView.findViewById(R.id.deviceText1)).setText("Type: " + parent.getType());
            ((TextView)convertView.findViewById(R.id.deviceText2)).setText("Frequency: " + parent.getFrequency() + " Hour Intervals");

            ImageView image = (ImageView)convertView.findViewById(R.id.stat_icon);
            image.setImageResource(R.drawable.box_blue);

            return convertView;
        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parentView) {
            final Device check = parents.get(groupPosition);
            final NameValuePair child = check.getData().get(childPosition);
            convertView = inflater.inflate(R.layout.childrow_device, parentView, false);

            assert convertView != null;
            ((TextView) convertView.findViewById(R.id.deviceText1)).setText(child.getName() + child.getValue());

            return convertView;
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
                getExpandableListView().collapseGroup(lastExpandedGroupPosition);
            }
            super.onGroupExpanded(groupPosition);
            lastExpandedGroupPosition = groupPosition;
        }
    }

    private class HttpGetTask extends AsyncTask<Void, Void, List<Device>> {
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(DeviceActivity.this, "Loading Information from Database.", "This may take a few seconds.", true, false, null);
            pd.setCancelable(true);
        }

        @Override
        protected List<Device> doInBackground(Void... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                return conn.getDevices();
            } catch (Exception e) {
                Log.e("DeviceActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Device> devices) {
            pd.dismiss();
            loadHosts((ArrayList)devices);
        }
    }

    private class HttpPostTask extends AsyncTask<Device, Void, Void> {
        @Override
        protected Void doInBackground(Device... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.postDevice(Params[0]);
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpPutTask extends AsyncTask<Device, Void, Void> {
        @Override
        protected Void doInBackground(Device... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.updateDevice(Params[0]);
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpDeleteTask extends AsyncTask<Device, Void, Void> {
        @Override
        protected Void doInBackground(Device... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.deleteDevice(Params[0]);
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }
}
