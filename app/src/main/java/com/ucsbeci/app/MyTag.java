package com.ucsbeci.app;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyTag extends ECIobj {

    private String tag_id;
    private int type_id;
    private String location;
    private String created;
    private String modified;
    private String lastmodifiedby;
    private String description;

    private String jsonText;

    public MyTag(JSONObject jo)
    {
        data = new ArrayList<NameValuePair>();
        try{
            this.id = jo.getInt("id");
            this.tag_id = jo.getString("tag_id");
            //this.type_id = jo.getInt("type_id");
            this.type_id = jo.getInt("type");
            this.location = jo.getString("location");
            this.created = jo.getString("created");
            this.modified = jo.getString("modified");
            this.lastmodifiedby = jo.getString("last_user");
            this.description = jo.getString("description");
        } catch (Exception e) {
            System.out.println("Could not parse JSON.");
            e.printStackTrace();
        }
        setEntity();
    }

    public MyTag() {
        this("",0,"","","","", "");
        setEntity();
    }

    public MyTag(String tag_id, int type_id, String location, String created, String modified, String by, String description)
    {
        data = new ArrayList<NameValuePair>();
        this.id = 0;
        this.tag_id = tag_id;
        this.type_id = type_id;
        this.location = location;
        this.created = created;
        this.modified = modified;
        this.lastmodifiedby = by;
        this.description = description;
        setEntity();
    }

    private void setEntity()
    {
        String obj = "{\"";
        obj += "id\":\"" + id + "\",\"";
        obj += "created\":\"" + created + "\",\"";
        obj += "description\":\"" + description + "\",\"";
        obj += "location\":\"" + location + "\",\"";
        obj += "modified\":\"" + modified + "\",\"";
        obj += "last_user\":\"" + lastmodifiedby + "\",\"";
        obj += "tag_id\":\"" + tag_id + "\",\"";
        obj += "type\":\"" + type_id + "\"";//obj += "type_id\":\"" + type_id + "\"";
        obj += "}";
        jsonText = obj;
        data.clear();
        data.add(new BasicNameValuePair("Resource Id: ", ""+id));
        data.add(new BasicNameValuePair("Tag Id: ", ""+tag_id));
        data.add(new BasicNameValuePair("Type Id: ", ""+type_id));
        data.add(new BasicNameValuePair("Created On: ", created));
        data.add(new BasicNameValuePair("Last Modified: ", modified));
        data.add(new BasicNameValuePair("\t\t\tBy: ", lastmodifiedby));
        data.add(new BasicNameValuePair("Delete this Entry?", ""));
    }

    public int getId() {
        return id;
    }

    public String getTag_id() {
        return tag_id;
    }

    public String getLocation() {
        return location;
    }

    public String getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
        setEntity();
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
        setEntity();
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
        setEntity();
    }

    public void setLocation(String location) {
        this.location = location;
        setEntity();
    }

    public void setCreated(String created) {
        this.created = created;
        setEntity();
    }

    public void setModified(String modified) {
        this.modified = modified;
        setEntity();
    }

    public void setLastUser(String last) {
        this.lastmodifiedby = last;
        setEntity();
    }

    public void setDescription(String description) {
        this.description = description;
        setEntity();
    }

    @Override
    public String toString() {
        return this.jsonText;
    }

}