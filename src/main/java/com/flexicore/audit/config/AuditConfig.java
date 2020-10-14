package com.flexicore.audit.config;

import com.flexicore.interfaces.ServicePlugin;
import org.pf4j.Extension;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Extension
@Configuration
@EnableAsync(proxyTargetClass = true)
public class AuditConfig implements ServicePlugin {
}
