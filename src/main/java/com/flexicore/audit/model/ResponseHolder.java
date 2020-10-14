package com.flexicore.audit.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class ResponseHolder {

    private Map<String,Object> response=new HashMap<>();

    public ResponseHolder() {
    }

    public ResponseHolder(Map<String, Object> response) {
        this.response = response;
    }

    @JsonAnyGetter
    public Map<String, Object> get() {
        return response;
    }
    @JsonAnySetter
    public void add(String key, Object value) {
        response.put(key,value);
    }
}
