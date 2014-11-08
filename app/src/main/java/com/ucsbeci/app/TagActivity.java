package com.ucsbeci.app;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class TagActivity extends ExpandableListActivity {

    private List<MyTag> parents = new ArrayList<MyTag>();
    private Context context;
    private ProgressDialog pd;

    private String toWrite = "";
    private NfcAdapter adapter;
    private PendingIntent pIntent;
    private TextView textView;
    IntentFilter[] tagFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        adapter = NfcAdapter.getDefaultAdapter(this);
        pIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagFilters = new IntentFilter[]{ tagDetected, new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED), new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED) };

        Resources res = getResources();
        Drawable line = res.getDrawable(R.drawable.line);

        getExpandableListView().setGroupIndicator(null);
        getExpandableListView().setDivider(line);
        getExpandableListView().setDividerHeight(2);
        getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (childPosition == parents.get(groupPosition).getData().size() - 1) {
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

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
            assert detectedTag != null;
            byte[] bytes = detectedTag.getId();
            char[] hexChars = new char[bytes.length * 2];
            int v;
            for ( int j = 0; j < bytes.length; j++ ) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            toWrite = new String(hexChars);
            if(textView != null) {
                textView.setText(toWrite);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.enableForegroundDispatch(this, pIntent, tagFilters, null);
    }

    @Override
    protected void onPause() {
        adapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new HttpGetTask().execute();
            loadHosts((ArrayList) parents);
            return true;
        } else if(id == R.id.action_edit) {
            editDialog();
        } else if (id == R.id.action_add_new) {
            MyTag tag = new MyTag();
            dialogSequence(tag);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialogSequence(final MyTag start) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(TagActivity.this);
        final LayoutInflater inflater = getLayoutInflater();

        final View nfcView = inflater.inflate(R.layout.dialog_promptnfc, null);
        builder1.setView(nfcView);

        assert nfcView != null;
        textView = (TextView) nfcView.findViewById(R.id.id_field);

        builder1.setTitle("Touch tag to write");
        builder1.setPositiveButton("I've got it!", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                for(MyTag t : parents) {
                    if(t.getTag_id().equals(((TextView) nfcView.findViewById(R.id.id_field)).getText())) {
                        Toast.makeText(context, "This tag already corresponds to tag #" + t.getId() +
                                "; Either find a new tag to write to or delete that resource to continue.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                AlertDialog.Builder builder2 = new AlertDialog.Builder(TagActivity.this);
                final View dialogView = inflater.inflate(R.layout.dialog_posttag, null);
                builder2.setView(dialogView);

                assert dialogView != null;
                TextView tagid = (TextView) dialogView.findViewById(R.id.tagid_input);
                if (!toWrite.equals("")) {
                    tagid.setText(((TextView) nfcView.findViewById(R.id.id_field)).getText());
                }

                List<Device> devices = new ArrayList<Device>();
                new HttpGetDeviceTask() {
                    @SuppressWarnings("ConstantConditions")
                    protected void onPostExecute(List<String> result) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, result);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setNotifyOnChange(true);

                        final Spinner typeid = (Spinner) dialogView.findViewById(R.id.type_input);
                        typeid.setAdapter(adapter);
                        typeid.setPrompt("Type of Device");

                    }
                }.execute(devices);

                List<Loc> locs = new ArrayList<Loc>();
                new HttpGetLocTask() {
                    @SuppressWarnings("ConstantConditions")
                    protected void onPostExecute(List<String> result) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, result);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setNotifyOnChange(true);

                        final Spinner locid = (Spinner) dialogView.findViewById(R.id.loc_input);
                        locid.setAdapter(adapter);
                        locid.setPrompt("Location of Tag");

                    }
                }.execute(locs);

                final List<Device> toAccess = devices;
                builder2.setTitle("New Tag Post");
                builder2.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Spinner loc = (Spinner) dialogView.findViewById(R.id.loc_input);
                        EditText descr = (EditText) dialogView.findViewById(R.id.description_input);
                        Spinner typeid = (Spinner) dialogView.findViewById(R.id.type_input);

                        start.setCreated(TimeFormatter.newTime());
                        start.setModified(start.getCreated());
                        start.setType_id(toAccess.get(typeid.getSelectedItemPosition()).getId());
                        start.setLocation(loc.getSelectedItem().toString());
                        start.setDescription(descr.getText().toString());
                        start.setTag_id(toWrite);

                        new HttpPostTask().execute(start);
                        new HttpGetTask().execute();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        toast("Cancelled Post Sequence");
                    }
                }).create().show();

            }
        });
        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                toast("Cancelled Post Sequence");
            }
        });
        builder1.create().show();
    }

    private void editDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(TagActivity.this);
        final LayoutInflater inflater = getLayoutInflater();

        final View nfcView = inflater.inflate(R.layout.dialog_promptnfc, null);
        builder1.setView(nfcView);

        assert nfcView != null;
        textView = (TextView) nfcView.findViewById(R.id.id_field);

        builder1.setTitle("Touch tag to write");
        builder1.setPositiveButton("I've got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Positive Button Clicked");
                MyTag thisTag = null;

                for(MyTag t : parents) {
                    if(t.getTag_id().equals(((TextView) nfcView.findViewById(R.id.id_field)).getText())) {
                        thisTag = t;
                    }
                }
                if(thisTag == null) {
                    Toast.makeText(context, "This tag has not been assigned yet. Feel free to make add it as a new tag.", Toast.LENGTH_LONG).show();
                    return;
                }
                final MyTag toEdit = thisTag;
                AlertDialog.Builder builder2 = new AlertDialog.Builder(TagActivity.this);
                final View dialogView = inflater.inflate(R.layout.dialog_posttag, null);
                builder2.setView(dialogView);

                assert dialogView != null;
                TextView tagid = (TextView) dialogView.findViewById(R.id.tagid_input);
                if (!toWrite.equals("")) {
                    tagid.setText(((TextView) nfcView.findViewById(R.id.id_field)).getText());
                }

                List<Device> devices = new ArrayList<Device>();
                new HttpGetDeviceTask() {
                    @SuppressWarnings("ConstantConditions")
                    protected void onPostExecute(List<String> result) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, result);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setNotifyOnChange(true);

                        final Spinner typeid = (Spinner) dialogView.findViewById(R.id.type_input);
                        typeid.setAdapter(adapter);
                        typeid.setSelection(Integer.parseInt(toEdit.getData().get(2).getValue()));

                    }
                }.execute(devices);

                List<Loc> locs = new ArrayList<Loc>();
                new HttpGetLocTask() {
                    @SuppressWarnings("ConstantConditions")
                    protected void onPostExecute(List<String> result) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(dialogView.getContext(), android.R.layout.simple_spinner_item, result);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setNotifyOnChange(true);

                        final Spinner locid = (Spinner) dialogView.findViewById(R.id.loc_input);
                        locid.setAdapter(adapter);
                        locid.setPrompt("Location of Tag");
                        for(int i = 0; i < result.size(); i++) {
                            if(result.get(i).equals(toEdit.getLocation())) {
                                locid.setSelection(i);
                                break;
                            }
                        }

                    }
                }.execute(locs);

                EditText description = (EditText) dialogView.findViewById(R.id.description_input);
                description.setText(toEdit.getDescription());
                String time = toEdit.getCreated();
                final String created = TimeFormatter.convertTime(time);

                final List<Device> toAccess = devices;
                builder2.setTitle("Edit Tag");
                builder2.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Spinner loc = (Spinner) dialogView.findViewById(R.id.loc_input);
                        EditText descr = (EditText) dialogView.findViewById(R.id.description_input);
                        Spinner typeid = (Spinner) dialogView.findViewById(R.id.type_input);

                        toEdit.setModified(TimeFormatter.newTime());
                        toEdit.setType_id(toAccess.get(typeid.getSelectedItemPosition()).getId());
                        toEdit.setLocation(loc.getSelectedItem().toString());
                        toEdit.setDescription(descr.getText().toString());
                        toEdit.setCreated(created);

                        new HttpPutTask().execute(toEdit);
                        new HttpGetTask().execute();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        toast("Cancelled Post Sequence");
                    }
                }).create().show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                toast("Cancelled Post Sequence");
            }
        }).create().show();

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
                toast("Record " + parents.get(groupPosition).getId() + " Deleted");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                toast("Deletion Aborted");
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tag, menu);
        return true;
    }

    private void loadHosts(final ArrayList<MyTag> newParents) {
        if (newParents == null) return;
        parents = newParents;

        if (this.getExpandableListAdapter() == null) {
            final MyExpandableListAdapter mAdapter = new MyExpandableListAdapter();
            this.setListAdapter(mAdapter);
        } else {
            ((MyExpandableListAdapter) getExpandableListAdapter()).notifyDataSetChanged();
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private int ParentClickStatus = -1;
        private int ChildClickStatus = -1;
        private LayoutInflater inflater;
        private int lastExpandedGroupPosition;

        public MyExpandableListAdapter() {
            // Create Layout Inflator
            inflater = LayoutInflater.from(TagActivity.this);
        }


        // This Function used to inflate parent rows view

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parentView) {
            final MyTag parent = parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow_tag, parentView, false);

            // Get grouprow.xml file elements and set values
            assert convertView != null;
            ((TextView) convertView.findViewById(R.id.tagText1)).setText("Description: " + parent.getDescription());
            ((TextView) convertView.findViewById(R.id.tagText2)).setText("Location: " + parent.getLocation());

            ImageView image = (ImageView) convertView.findViewById(R.id.stat_icon);
            image.setImageResource(R.drawable.box_gray);

            return convertView;
        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parentView) {
            final MyTag tag = parents.get(groupPosition);
            final NameValuePair child = tag.getData().get(childPosition);
            convertView = inflater.inflate(R.layout.childrow_tag, parentView, false);

            assert convertView != null;
            ((TextView) convertView.findViewById(R.id.tagText1)).setText(child.getName() + child.getValue());

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
            if (ParentClickStatus == 0) {
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
            if (groupPosition != lastExpandedGroupPosition) {
                getExpandableListView().collapseGroup(lastExpandedGroupPosition);
            }
            super.onGroupExpanded(groupPosition);
            lastExpandedGroupPosition = groupPosition;
        }
    }

    private class HttpGetTask extends AsyncTask<Void, Void, List<MyTag>> {
        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(TagActivity.this, "Loading Information from Database.", "This may take a few seconds.", true, false, null);
            pd.setCancelable(true);
        }

        @Override
        protected List<MyTag> doInBackground(Void... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                return conn.getTags();
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MyTag> tags) {
            pd.dismiss();
            loadHosts((ArrayList) tags);
        }
    }

    private class HttpPostTask extends AsyncTask<MyTag, Void, Void> {
        @Override
        protected Void doInBackground(MyTag... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.postTag(Params[0]);
            } catch (Exception e) {
                Log.e("TagActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpPutTask extends AsyncTask<MyTag, Void, Void> {
        @Override
        protected Void doInBackground(MyTag... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.updateTag(Params[0]);
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpDeleteTask extends AsyncTask<MyTag, Void, Void> {

        @Override
        protected Void doInBackground(MyTag... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.deleteTag(Params[0]);
            } catch (Exception e) {
                Log.e("TagActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpGetDeviceTask extends AsyncTask<List<Device>, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(TagActivity.this, "Loading Information from Database.", "This may take a few seconds.", true, false, null);
            pd.setCancelable(true);
        }

        @Override
        protected List<String> doInBackground(List<Device>... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                List<Device> devices = conn.getDevices();
                List<String> types = new ArrayList<String>();
                for (Device d : devices) {
                    Params[0].add(d);
                    types.add(d.getId() + " - " + d.getType());
                }
                return types;
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            pd.dismiss();
        }

    }

    private class HttpGetLocTask extends AsyncTask<List<Loc>, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(TagActivity.this, "Loading Information from Database.", "This may take a few seconds.", true, false, null);
            pd.setCancelable(true);
        }

        @Override
        protected List<String> doInBackground(List<Loc>... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                List<Loc> locs = conn.getLocations();
                List<String> types = new ArrayList<String>();
                for (Loc l : locs) {
                    Params[0].add(l);
                    types.add(l.getLocation());
                }
                return types;
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            pd.dismiss();
        }

    }

}
