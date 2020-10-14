package com.flexicore.audit.rest;

import com.flexicore.model.Baseclass;
import com.flexicore.test.EntityProviderClasses;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import java.util.Collections;
import java.util.HashSet;

@Configuration
public class EntitiesSeedProvider {

    @Primary
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public EntityProviderClasses getSeeds() {
        return new EntityProviderClasses(new HashSet<>(Collections.singletonList(Baseclass.class)));

    }
}
