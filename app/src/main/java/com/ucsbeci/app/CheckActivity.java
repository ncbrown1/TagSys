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

public class CheckActivity extends ExpandableListActivity {
    private String mUser;

    private List<Check> parents = new ArrayList<Check>();
    private List<MyTag> tags = new ArrayList<MyTag>();
    private Context context;

    private ProgressDialog pd;

    private NfcAdapter adapter;
    private PendingIntent pIntent;
    IntentFilter[] tagFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getIntent().getExtras().getString("Username");
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
                if(childPosition == parents.get(groupPosition).getData().size()-1) {
                    deleteDialog(groupPosition);
                    return true;
                }
                return false;
            }
        });
        registerForContextMenu(getExpandableListView());

        new HttpGetTagTask().execute(tags);
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
            String uid = new String(hexChars);
            MyTag corrTag = findTag(uid);
            if(corrTag == null) {
                AlertDialog.Builder notify = new AlertDialog.Builder(this);
                notify.setTitle("Warning!");
                notify.setMessage("This tag does not correspond to anything in the database.\nPlease create a new resource in the Tags database in order to check in here.");
                notify.create().show();
            } else {
                dialogSequence(new Check(), corrTag);
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

    private void loadHosts(final ArrayList<Check> newParents)
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
        getMenuInflater().inflate(R.menu.check, menu);
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
            return true;
        } else if (id == R.id.action_add_new) {
            AlertDialog.Builder promptForNfc = new AlertDialog.Builder(this);
            promptForNfc.setTitle("Touch tag to check in");
            promptForNfc.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialogSequence(final Check start, final MyTag myTag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CheckActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog_postcheck, null);
        builder.setView(dialogView);

        assert dialogView != null;
        TextView text = (TextView) dialogView.findViewById(R.id.tagid_input);
        text.setText(myTag.getTag_id() + " - " + myTag.getId() + " - " + myTag.getLocation() + " - " + myTag.getDescription());

        builder.setTitle("New Check-In Post");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(DialogInterface dialog, int i) {
                EditText notes = (EditText) dialogView.findViewById(R.id.notes_input);
                Spinner status = (Spinner) dialogView.findViewById(R.id.status_spinner);

                String now = TimeFormatter.newTime();
                start.setTime(now);
                start.setTag_id(myTag.getId());
                start.setUser(mUser);
                start.setNotes(notes.getText().toString());
                start.setStatus(status.getSelectedItem().toString());

                String created = myTag.getCreated();
                created = TimeFormatter.convertTime(created);
                myTag.setCreated(created);
                myTag.setModified(now);
                myTag.setLastUser(mUser);

                new EmailTask(start, myTag).execute();
                new HttpPostTask().execute(start);
                new HttpPostTagTask().execute(myTag);
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

    private MyTag findTagWithId(int id) {
        for(MyTag t : tags) {
            if(t.getId() == id) {
                return t;
            }
        }
        return null;
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
            inflater = LayoutInflater.from(CheckActivity.this);
        }


        // This Function used to inflate parent rows view
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parentView)
        {
            final Check parent = parents.get(groupPosition);

            // Inflate grouprow.xml file for parent rows
            convertView = inflater.inflate(R.layout.grouprow_check, parentView, false);

            // Get grouprow.xml file elements and set values
            assert convertView != null;
            String note = parent.getNotes();
            ((TextView)convertView.findViewById(R.id.checkText1)).setText(note.equals("null") || note.equals("") ? "Notes: <Not Available>" : "Notes: " + note);

            int i = parent.getTag_id();
            ((TextView)convertView.findViewById(R.id.checkText2)).setText("Tag Id: " + i + " - " + findTagWithId(i).getLocation() + " - " + findTagWithId(i).getDescription());

            ImageView image = (ImageView)convertView.findViewById(R.id.stat_icon);
            if(parent.getStatus().equals("good")) image.setBackgroundResource(R.drawable.box_green);
            else if(parent.getStatus().equals("warning")) image.setBackgroundResource(R.drawable.box_orange);
            else image.setBackgroundResource(R.drawable.box_red);

            return convertView;
        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parentView) {
            final Check check = parents.get(groupPosition);
            final NameValuePair child = check.getData().get(childPosition);
            convertView = inflater.inflate(R.layout.childrow_check, parentView, false);

            assert convertView != null;
            ((TextView) convertView.findViewById(R.id.checkText1)).setText(child.getName() + child.getValue());

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

    private class HttpGetTask extends AsyncTask<Void, Void, List<Check>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(CheckActivity.this, "Loading Information from Database.", "This may take a few seconds.", true, false, null);
            pd.setCancelable(true);
        }

        @Override
        protected List<Check> doInBackground(Void... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                return conn.getChecks();
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Check> checks) {
            pd.dismiss();
            loadHosts((ArrayList)checks);
        }
    }

    private class HttpPostTask extends AsyncTask<Check, Void, Void> {
        @Override
        protected Void doInBackground(Check... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.postCheck(Params[0]);
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class EmailTask extends AsyncTask<Void, Void, Void> {
        private Check check;
        private MyTag tag;
        public EmailTask(Check c, MyTag t) {
            this.check = c;
            this.tag = t;
        }

        @Override
        protected Void doInBackground(Void... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.sendMail(check, tag);
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpDeleteTask extends AsyncTask<Check, Void, Void> {
        @Override
        protected Void doInBackground(Check... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                conn.deleteCheck(Params[0]);
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpGetTagTask extends AsyncTask<List<MyTag>, Void, List<String>> {

        @Override
        protected List<String> doInBackground(List<MyTag>... Params) {
            try {
                HttpConnector conn = new HttpConnector();
                List<MyTag> myTags = conn.getTags();
                tags = myTags;
                List<String> ids = new ArrayList<String>();
                for(MyTag t : myTags) {
                    Params[0].add(t);
                    ids.add(t.getId() + " - " + t.getLocation() + " - " + t.getDescription());
                }
                return ids;
            } catch (Exception e) {
                Log.e("CheckActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    private class HttpPostTagTask extends AsyncTask<MyTag, Void, Void> {
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

    private MyTag findTag(String uid) {
        for(MyTag t : tags) {
            if(t.getTag_id().equals(uid)) {
                return t;
            }
        }
        return null;
    }

}
