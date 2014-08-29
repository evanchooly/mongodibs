package com.mongodb.dibs.auth;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.auth.Authenticator;

public class OpenIDRestrictedToProvider<T> implements InjectableProvider<Restricted, Parameter> {

  private final Authenticator<OpenIDCredentials, T> authenticator;
  private final String realm;

  /**
   * Creates a new {@link OpenIDRestrictedToProvider} with the given {@link Authenticator} and realm.
   *
   * @param authenticator the authenticator which will take the {@link OpenIDCredentials} and
   *                      convert them into instances of {@code T}
   * @param realm         the name of the authentication realm
   */
  public OpenIDRestrictedToProvider(Authenticator<OpenIDCredentials, T> authenticator, String realm) {
    this.authenticator = authenticator;
    this.realm = realm;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }

  @Override
  public Injectable<?> getInjectable(ComponentContext ic, Restricted a, Parameter c) {
      return new OpenIDRestrictedToInjectable<T>(authenticator, realm, a.value());
  }
}
