/*******************************************************************************
 *  Copyright (C) FlexiCore, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Avishay Ben Natan And Asaf Ben Natan, October 2015
 ******************************************************************************/
package com.flexicore.audit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexicore.annotations.plugins.PluginInfo;
import com.flexicore.audit.data.AuditingRepository;
import com.flexicore.audit.model.*;
import com.flexicore.audit.request.AuditingFilter;
import com.flexicore.data.jsoncontainers.ObjectMapperContextResolver;
import com.flexicore.data.jsoncontainers.PaginationResponse;
import com.flexicore.interfaces.ServicePlugin;
import com.flexicore.model.Operation;
import com.flexicore.model.User;
import com.flexicore.security.SecurityContext;
import com.flexicore.service.BaseclassNewService;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Extension
@PluginInfo(version = 1)
@Component
public class AuditingService implements ServicePlugin {

    private static final Logger logger = LoggerFactory.getLogger(AuditingService.class);

    @Autowired
    @PluginInfo(version = 1)
    private AuditingRepository auditingRepository;

    @Autowired
    private BaseclassNewService baseclassNewService;


    @Async
    @EventListener
    public void handleAuditingEvent(AuditingEvent auditingEvent) {
        auditingRepository.merge(auditingEvent);
        logger.info("Call to " + auditingEvent.getOperationHolder() + " was Audited");
    }

    @Async
    @EventListener
    public void createAuditingEvent(AuditingJob auditingJob) {
        ObjectMapper objectMapper = ObjectMapperContextResolver.getDefaultMapper();
        Stream<Object> parameters = Stream.of(auditingJob.getParameters());


        List<Object> list = parameters.filter(Objects::nonNull).collect(Collectors.toList());


        SecurityContext securityContext = auditingJob.getSecurityContext();
        List<ParameterHolder> parameterHolders = auditingJob.getParameters().stream().filter(f -> !(f instanceof SecurityContext)).map(f -> new ParameterHolder(mapToMap(f))).collect(Collectors.toList());
        AuditingEvent auditingEvent = new AuditingEvent()
                .setDateOccurred(auditingJob.getDateOccured())
                .setOperationHolder(securityContext != null && securityContext.getOperation() != null ? new OperationHolder(securityContext.getOperation()) : null)
                .setUserHolder(securityContext != null && securityContext.getUser() != null ? new UserHolder(securityContext.getUser()) : null)
                .setRequest(new RequestHolder().setParameters(parameterHolders))
                .setTimeTaken(auditingJob.getTimeTaken())
                .setAuditingType(auditingJob.getAuditingType())
                .setFailed(auditingJob.isFailed());
        if (auditingJob.getResponse() != null) {
            auditingEvent.setResponse(new ResponseHolder(mapToMap(auditingJob.getResponse())));
        }
        auditingRepository.merge(auditingEvent);
        logger.info("Call to " + auditingEvent.getOperationHolder() + " was Audited");
    }

    private Map<String, Object> mapToMap(Object response) {
        if (response instanceof String) {
            Map<String, Object> toRet = new HashMap<>();

            toRet.put("stringValue", response);
            return toRet;
        } else {
            if (response instanceof Integer || response instanceof Float || response instanceof Double || response instanceof Long) {
                Map<String, Object> toRet = new HashMap<>();

                toRet.put("numericValue", response);
                return toRet;
            } else {
                TypeReference<Map<String, Object>> t = new TypeReference<Map<String, Object>>() {
                };
                return ObjectMapperContextResolver.getDefaultMapper().convertValue(response, t);
            }
        }


    }

    private boolean isFirstAuthToken(Parameter[] parameters) {
        if (parameters.length > 0) {
            Parameter parameter = parameters[0];
            HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
            if (headerParam != null) {
                return "authenticationkey".toLowerCase().equals(headerParam.value().toLowerCase());
            }
            PathParam pathParam = parameter.getAnnotation(PathParam.class);

            if (pathParam != null) {
                return "authenticationkey".toLowerCase().equals(pathParam.value().toLowerCase());
            }

            QueryParam queryParam = parameter.getAnnotation(QueryParam.class);

            if (queryParam != null) {
                return "authenticationkey".toLowerCase().equals(queryParam.value().toLowerCase());
            }
        }
        return false;
    }

    public PaginationResponse<AuditingEvent> getAllAuditingEvents(AuditingFilter auditingFilter, SecurityContext securityContext) {
        List<AuditingEvent> auditingEvents = auditingRepository.listAllAuditingEvents(auditingFilter);
        long count = auditingRepository.countAllAuditingEvents(auditingFilter);
        return new PaginationResponse<>(auditingEvents, auditingFilter, count);
    }

    public void validate(AuditingFilter auditingFilter, SecurityContext securityContext) {
        Set<String> operationIds = auditingFilter.getOperationIds().stream().map(f -> f.getId()).collect(Collectors.toSet());
        Map<String, Operation> operationMap = operationIds.isEmpty() ? new HashMap<>() : baseclassNewService.listByIds(Operation.class, operationIds, securityContext).stream().collect(Collectors.toMap(f -> f.getId(), f -> f));
        operationIds.removeAll(operationMap.keySet());
        if (!operationIds.isEmpty()) {
            throw new BadRequestException("No Operations with ids " + operationIds);
        }
        auditingFilter.setOperations(new ArrayList<>(operationMap.values()));

        Set<String> userIds = auditingFilter.getUserIds().stream().map(f -> f.getId()).collect(Collectors.toSet());
        Map<String, User> userMap = userIds.isEmpty() ? new HashMap<>() : baseclassNewService.listByIds(User.class, userIds, securityContext).stream().collect(Collectors.toMap(f -> f.getId(), f -> f));
        userIds.removeAll(userMap.keySet());
        if (!userIds.isEmpty()) {
            throw new BadRequestException("No User with ids " + userIds);
        }
        auditingFilter.setUsers(new ArrayList<>(userMap.values()));
    }

}
