package com.flexicore.audit.invokers;

import com.flexicore.annotations.plugins.PluginInfo;
import com.flexicore.audit.model.AuditingEvent;
import com.flexicore.audit.request.AuditingFilter;
import com.flexicore.audit.service.AuditingService;
import com.flexicore.data.jsoncontainers.PaginationResponse;
import com.flexicore.interfaces.dynamic.InvokerInfo;
import com.flexicore.interfaces.dynamic.InvokerMethodInfo;
import com.flexicore.interfaces.dynamic.ListingInvoker;
import com.flexicore.security.SecurityContext;
import org.pf4j.Extension;

import javax.inject.Inject;

@PluginInfo(version = 1)
@InvokerInfo
@Extension
public class AuditingInvoker implements ListingInvoker<AuditingEvent, AuditingFilter> {

    @Inject
    @PluginInfo(version = 1)
    private AuditingService auditingEventService;

    @Override
    @InvokerMethodInfo(displayName = "listAllAuditingEvents",description = "lists all auditingEvents")
    public PaginationResponse<AuditingEvent> listAll(AuditingFilter filter, SecurityContext securityContext) {
        auditingEventService.validate(filter,securityContext);
        return auditingEventService.getAllAuditingEvents(filter,securityContext);
    }

    @Override
    public Class<AuditingFilter> getFilterClass() {
        return AuditingFilter.class;
    }

    @Override
    public Class<?> getHandlingClass() {
        return AuditingEvent.class;
    }
}
