package com.mongodb.dibs.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.mongodb.dibs.DibsConfiguration;
import com.mongodb.dibs.auth.Authority;
import com.mongodb.dibs.auth.InMemoryUserCache;
import com.mongodb.dibs.auth.User;
import com.mongodb.dibs.model.OAuthConfig;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/auth")
@Produces(MediaType.TEXT_HTML)
public class PublicOAuthResource {

    private static final Logger log = LoggerFactory.getLogger(PublicOAuthResource.class);
    public static final String AUTH_MANAGER = "authManager";
    private final DibsConfiguration dibsConfiguration;

    public PublicOAuthResource(DibsConfiguration dibsConfiguration) {
        this.dibsConfiguration = dibsConfiguration;
    }

    @GET
    @Path("/login")
    public Response requestOAuth(@Context HttpServletRequest request) throws URISyntaxException {
        List<OAuthConfig> oauthCfg = dibsConfiguration.getOAuthCfg();
        if (oauthCfg != null) {
            try {
                SocialAuthManager manager = getSocialAuthManager();

                request.getSession().setAttribute(AUTH_MANAGER, manager);

                final URI uri = new URI(manager.getAuthenticationUrl("googleplus", dibsConfiguration.getOAuthSuccessUrl()));
                return Response
                           .temporaryRedirect(uri)
                           .build();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    /**
     * Handles the OAuth server response to the earlier AuthRequest
     *
     * @return The OAuth identifier for this user if verification was successful
     */
    @GET
    @Timed
    @Path("/verify")
    public Response verifyOAuthServerResponse(@Context HttpServletRequest request) {
        SocialAuthManager manager = (SocialAuthManager) request.getSession().getAttribute(AUTH_MANAGER);

        if (manager != null) {
            try {
                Map<String, String> params = SocialAuthUtil.getRequestParametersMap(request);
                AuthProvider provider = manager.connect(params);

                Profile p = provider.getUserProfile();

                log.info("Logging in user '{}'", p);

                User tempUser = new User(UUID.randomUUID());
                tempUser.setOpenIDIdentifier(p.getValidatedId());
                tempUser.setOAuthInfo(provider.getAccessGrant());

                tempUser.setEmail(p.getEmail());

                tempUser.getAuthorities().add(Authority.ROLE_PUBLIC);
                if ((dibsConfiguration.getAdminUsers() != null) && (tempUser.getEmail() != null)) {
                    Map<String, String> adminUsers = dibsConfiguration.getAdminUsers();
                    if (adminUsers.containsKey(tempUser.getEmail())
                        && (adminUsers.get(tempUser.getEmail()).equals(provider.getProviderId()))) {
                        tempUser.getAuthorities().add(Authority.ROLE_ADMIN);
                    }
                }
                
                Optional<User> userOptional = InMemoryUserCache.INSTANCE.getByOpenIDIdentifier(tempUser.getOpenIDIdentifier());
                if (!userOptional.isPresent()) {
                    InMemoryUserCache.INSTANCE.put(tempUser.getSessionToken(), tempUser);
                } else {
                    tempUser = userOptional.get();
                }

                return Response.temporaryRedirect(new URI("/"))
                           .cookie(replaceSessionTokenCookie(Optional.of(tempUser)))
                           .build();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        // Must have failed to be here
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    /**
     * Gets an initialized SocialAuthManager
     *
     * @return gets an initialized SocialAuthManager
     */
    private SocialAuthManager getSocialAuthManager() {
        SocialAuthConfig config = SocialAuthConfig.getDefault();
        try {
            config.load(dibsConfiguration.getOAuthCfgProperties());
            SocialAuthManager manager = new SocialAuthManager();
            manager.setSocialAuthConfig(config);
            return manager;
        } catch (Exception e) {
            log.error("SocialAuth error: " + e);
        }
        return null;
    }

    protected NewCookie replaceSessionTokenCookie(Optional<User> user) {
        if (user.isPresent()) {
            String value = user.get().getSessionToken().toString();
            log.debug("Replacing session token with {}", value);
            return new NewCookie(DibsConfiguration.SESSION_TOKEN_NAME, value, "/", null, null, 86400 * 30, false);
        } else {
            // Remove the session token cookie
            log.debug("Removing session token");
            return new NewCookie(DibsConfiguration.SESSION_TOKEN_NAME, null, null, null, null, 0, false);
        }
    }

}