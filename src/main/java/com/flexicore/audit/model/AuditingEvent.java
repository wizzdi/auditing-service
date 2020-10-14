package com.flexicore.audit.model;


import com.flexicore.model.Baseclass;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Date;

public class AuditingEvent {
    @BsonId
    private String id;
    private OperationHolder operationHolder;
    private Date dateOccurred;
    private UserHolder userHolder;
    private long timeTaken;
    private RequestHolder request;
    private ResponseHolder response;
    private String auditingType;
    private boolean failed;

    public AuditingEvent() {
        this.id=Baseclass.getBase64ID();
    }


    public String getId() {
        return id;
    }

    public <T extends AuditingEvent> T setId(String id) {
        this.id = id;
        return (T) this;
    }

    public OperationHolder getOperationHolder() {
        return operationHolder;
    }

    public <T extends AuditingEvent> T setOperationHolder(OperationHolder operationHolder) {
        this.operationHolder = operationHolder;
        return (T) this;
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }

    public <T extends AuditingEvent> T setDateOccurred(Date dateOccurred) {
        this.dateOccurred = dateOccurred;
        return (T) this;
    }

    public UserHolder getUserHolder() {
        return userHolder;
    }

    public <T extends AuditingEvent> T setUserHolder(UserHolder userHolder) {
        this.userHolder = userHolder;
        return (T) this;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public <T extends AuditingEvent> T setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
        return (T) this;
    }

    public RequestHolder getRequest() {
        return request;
    }

    public <T extends AuditingEvent> T setRequest(RequestHolder request) {
        this.request = request;
        return (T) this;
    }

    public ResponseHolder getResponse() {
        return response;
    }

    public <T extends AuditingEvent> T setResponse(ResponseHolder response) {
        this.response = response;
        return (T) this;
    }

    public String getAuditingType() {
        return auditingType;
    }

    public <T extends AuditingEvent> T setAuditingType(String auditingType) {
        this.auditingType = auditingType;
        return (T) this;
    }

    public boolean isFailed() {
        return failed;
    }

    public <T extends AuditingEvent> T setFailed(boolean failed) {
        this.failed = failed;
        return (T) this;
    }
}
