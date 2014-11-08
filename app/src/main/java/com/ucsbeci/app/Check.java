package com.ucsbeci.app;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

public class Check extends ECIobj {

    private int tag_id;
    private Object time;
    private String user;
    private String notes;
    private String status;

    private String jsonText;

    public Check(JSONObject jo)
    {
        data = new ArrayList<NameValuePair>();
        try{
            this.id = jo.getInt("id");
            this.tag_id = jo.getInt("tag");
            this.time = jo.get("time");
            this.user = jo.getString("user");
            this.notes = jo.getString("notes");
            this.status = jo.getString("status");
        } catch (Exception e) {
            System.out.println("Could not parse JSON.");
            e.printStackTrace();
        }
        setEntity();
    }

    public Check()
    {
        this(0,0,"","","","");
        setEntity();
    }

    public Check(int id, int tag_id, String time, String user, String notes, String status) {
        data = new ArrayList<NameValuePair>();
        this.id = id;
        this.tag_id = tag_id;
        this.time = time;
        this.user = user;
        this.notes = notes;
        this.status = status;
        setEntity();
    }

    private void setEntity()
    {
        String obj = "{\"";
        obj += "id\": " + id + ", \"";
        obj += "notes\": \"" + notes + "\", \"";
        obj += "status\": \"" + status + "\", \"";
        obj += "tag\": \"" + tag_id + "\", \"";
        obj += "time\": \"" + time + "\", \"";
        obj += "user\": \"" + user + "\"";
        obj += "}";
        jsonText = obj;
        data.add(new BasicNameValuePair("Date: ", time.toString()));
        data.add(new BasicNameValuePair("User: ", user));
        data.add(new BasicNameValuePair("Delete this Entry?", ""));
    }

    public void setId(int id) {
        this.id = id;
        setEntity();
    }

    public void setTag_id(int tag_id) {
        this.tag_id = tag_id;
        setEntity();
    }

    public void setTime(String time) {
        this.time = time;
        setEntity();
    }

    public void setUser(String user) {
        this.user = user;
        setEntity();
    }

    public void setNotes(String notes) {
        this.notes = notes;
        setEntity();
    }

    public void setStatus(String status) {
        this.status = status;
        setEntity();
    }

    public int getId() {
        return id;
    }
    public int getTag_id() {
        return tag_id;
    }
    public String getNotes() {
        return notes;
    }
    public String getStatus() {
        return status;
    }
    public String getUser() { return user; }

    @Override
    public String toString() {
        return jsonText;
    }
}