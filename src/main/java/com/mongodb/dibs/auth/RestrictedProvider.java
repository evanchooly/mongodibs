package com.mongodb.dibs.auth;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

public class RestrictedProvider implements InjectableProvider<Restricted, Parameter> {

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, Restricted a, Parameter c) {
        return new OpenIDRestrictedToInjectable(a.value());
    }
}
