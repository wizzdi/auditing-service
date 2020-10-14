package com.flexicore.audit.model;

import java.util.List;

public class RequestHolder {

    private List<ParameterHolder> parameters;

    public List<ParameterHolder> getParameters() {
        return parameters;
    }

    public <T extends RequestHolder> T setParameters(List<ParameterHolder> parameters) {
        this.parameters = parameters;
        return (T) this;
    }
}
