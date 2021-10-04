package com.wrox.config.bootstrap;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@SuppressWarnings("unused")
@Order(2)
public class SecurityBootstrap extends AbstractSecurityWebApplicationInitializer
{
    @Override
    protected boolean enableHttpSessionEventPublisher()
    {
        return true;
    }
}
