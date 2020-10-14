/*******************************************************************************
 *  Copyright (C) FlexiCore, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Avishay Ben Natan And Asaf Ben Natan, October 2015
 ******************************************************************************/
package com.flexicore.audit.rest;

import com.flexicore.annotations.OperationsInside;
import com.flexicore.annotations.ProtectedREST;
import com.flexicore.annotations.plugins.PluginInfo;
import com.flexicore.audit.model.AuditingEvent;
import com.flexicore.audit.request.AuditingFilter;
import com.flexicore.audit.service.AuditingService;
import com.flexicore.data.jsoncontainers.PaginationResponse;
import com.flexicore.interfaces.RestServicePlugin;
import com.flexicore.security.SecurityContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/audit")
@RequestScoped
@Component
@OperationsInside
@ProtectedREST
@Tag(name = "Audit")
@Extension
@PluginInfo(version = 1)
public class AuditRESTService implements RestServicePlugin {


	@Autowired
	@PluginInfo(version = 1)
	private AuditingService service;


	@POST
	@Produces("application/json")
	@Operation(summary = "getAllAuditingEvents", description = "lists all auditingEvents")
	@Path("getAllAuditingEvents")
	public PaginationResponse<AuditingEvent> getAllAuditingEvents(
			@HeaderParam("authenticationKey") String authenticationKey,
			AuditingFilter auditingFilter,
			@Context SecurityContext securityContext) {
		service.validate(auditingFilter,securityContext);
		return service.getAllAuditingEvents(auditingFilter, securityContext);
	}



}
