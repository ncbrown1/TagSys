package com.ucsbeci.app;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

public class Loc extends ECIobj {

    private String location;
    private String jsonText;

    public Loc(JSONObject jo) {
        data = new ArrayList<NameValuePair>();
        try {
            this.id = jo.getInt("id");
            this.location = jo.getString("location");
        } catch (Exception e) {
            System.out.println("Could not parse JSON");
            e.printStackTrace();
        }
        setEntity();
    }

    public Loc() {
        this(0,"");
        setEntity();
    }

    public Loc(int id, String loc) {
        data = new ArrayList<NameValuePair>();
        this.id = id;
        this.location = loc;
        setEntity();
    }

    private void setEntity() {
        String obj = "{\"";
        obj += "id\":\"" + id + "\",\"";
        obj += "location\":\"" + location + "\"";
        obj += "}";
        jsonText = obj;
        data.add(new BasicNameValuePair("Delete this Entry?", ""));
    }

    public int getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public void setId(int id) {
        this.id = id;
        setEntity();
    }

    public void setLocation(String location) {
        this.location = location;
        setEntity();
    }

    @Override
    public String toString() {
        return this.jsonText;
    }
}
