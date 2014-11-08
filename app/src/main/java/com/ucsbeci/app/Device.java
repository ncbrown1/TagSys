package com.ucsbeci.app;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

public class Device extends ECIobj {

    private String type;
    private String characteristics;
    private int frequency;

    private String jsonText;

    public Device(JSONObject jo)
    {
        data = new ArrayList<NameValuePair>();
        try {
            id = jo.getInt("id");
            type = jo.getString("type");
            characteristics = jo.getString("characteristics");
            frequency = jo.getInt("frequency");
        } catch (Exception e) {
            System.out.println("Could not parse JSON.");
            e.printStackTrace();
        }
        setEntity();
    }

    public Device()
    {
        this(0,"","",0);
        setEntity();
    }

    public Device(int id, String type, String characteristics, int frequency) {
        data = new ArrayList<NameValuePair>();
        this.id = id;
        this.type = type;
        this.characteristics = characteristics;
        this.frequency = frequency;
        setEntity();
    }

    private void setEntity()
    {
        String obj = "{ \"";
        obj += "id\": \"" + id + "\", \"";
        obj += "characteristics\": \"" + characteristics + "\", \"";
        obj += "frequency\": \"" + frequency + "\", \"";
        obj += "type\": \"" + type + "\"";
        obj += "}";
        jsonText = obj;
        data.add(new BasicNameValuePair("Id #: ", "" + id));
        data.add(new BasicNameValuePair("Characteristics: ", "" + characteristics));
        data.add(new BasicNameValuePair("Edit this Entry?", ""));
        data.add(new BasicNameValuePair("Delete this Entry?", ""));
    }

    public void setId(int id) {
        this.id = id;
        setEntity();
    }

    public void setType(String type) {
        this.type = type;
        setEntity();
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
        setEntity();
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
        setEntity();
    }

    public int getId() { return id; }
    public String getType() {
        return type;
    }
    public int getFrequency() {  return frequency; }

    @Override
    public String toString() {
        return jsonText;
    }
}