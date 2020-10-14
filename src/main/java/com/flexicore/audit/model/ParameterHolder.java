package com.flexicore.audit.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class ParameterHolder {

    private Map<String,Object> request=new HashMap<>();

    public ParameterHolder(Map<String, Object> request) {
        this.request = request;
    }

    public ParameterHolder() {
    }

    @JsonAnyGetter
    public Map<String, Object> get() {
        return request;
    }
    @JsonAnySetter
    public void add(String key, Object value) {
        request.put(key,value);
    }
}
