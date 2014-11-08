package com.ucsbeci.app;

import org.apache.http.NameValuePair;

import java.util.List;

public abstract class ECIobj {

    protected int id;
    protected List<NameValuePair> data;

    public int getId() {
        return id;
    }
    public List<NameValuePair> getData() { return data; }

}
