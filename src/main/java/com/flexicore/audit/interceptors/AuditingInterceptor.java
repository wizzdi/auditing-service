/*******************************************************************************
 *  Copyright (C) FlexiCore, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Avishay Ben Natan And Asaf Ben Natan, October 2015
 ******************************************************************************/
package com.flexicore.audit.interceptors;

import com.flexicore.audit.model.AuditingJob;
import com.flexicore.audit.model.DefaultAuditingTypes;
import com.flexicore.interfaces.AspectPlugin;
import com.flexicore.security.SecurityContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * uses Aspect Oriented Programming (through JavaEE support) to enforce security
 * Access granularity is specified in a separate UML diagram
 *
 * @author Avishay Ben Natan
 */


@Aspect
@Component
@Extension
@Order(200)
public class AuditingInterceptor implements AspectPlugin {

    private static final Logger logger = LoggerFactory.getLogger(AuditingInterceptor.class);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Around("execution(@com.flexicore.annotations.Protected * *(..)) || within(@(@com.flexicore.annotations.Protected *) *)|| within(@com.flexicore.annotations.Protected *)")
    public Object transformReturn(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] parameters = joinPoint.getArgs();
        String methodName = method.getName();
        Session  websocketSession = getWebsocketSession(parameters);
        SecurityContext securityContext;
        if (websocketSession != null) {
            securityContext = (SecurityContext) websocketSession.getUserProperties().get("securityContext");
        } else {
            securityContext = (SecurityContext) parameters[parameters.length - 1];

        }
        boolean auditable = securityContext.getOperation().isAuditable();
        Object response=null;
        try {

            response = joinPoint.proceed(parameters);
            return response;
        }
        finally {
            if(auditable){
                long time=System.currentTimeMillis()-start;
                List<Object> parametersList = Stream.of(parameters).collect(Collectors.toList());
                applicationEventPublisher.publishEvent(new AuditingJob(securityContext, parametersList,response,time,new Date(), DefaultAuditingTypes.REST.name()));
                logger.debug("operation "+methodName+" was audited");
            }
        }


    }

    private Session getWebsocketSession(Object[] parameters) {
        return parameters != null ? Stream.of(parameters).filter(f -> f instanceof Session).map(f -> (Session) f).findAny().orElse(null) : null;
    }


}
