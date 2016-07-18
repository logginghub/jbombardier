package com.jbombardier;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 17/03/15.
 */
public class ControllerResultSnapshot {
    private String name;
    private String source;
    private List<JsonObject> results = new ArrayList<JsonObject>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<JsonObject> getResults() {
        return results;
    }

    public void setResults(List<JsonObject> results) {
        this.results = results;
    }
}
