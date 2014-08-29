package com.mongodb.dibs;

import com.mongodb.dibs.resources.PublicErrorResource;
import com.sun.jersey.api.core.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger log = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    @Context
    HttpContext httpContext;

    @Override
    public Response toResponse(RuntimeException runtime) {

        // Build default response
        Response defaultResponse = Response
                                       .serverError()
                                       .entity(new PublicErrorResource().view500())
                                       .build();

        // Check for any specific handling
        if (runtime instanceof WebApplicationException) {

            return handleWebApplicationException(runtime, defaultResponse);
        }

        // Use the default
        log.error(runtime.getMessage(), runtime);
        return defaultResponse;

    }

    private Response handleWebApplicationException(RuntimeException exception, Response defaultResponse) {
        WebApplicationException webAppException = (WebApplicationException) exception;

        // No logging
        int status = webAppException.getResponse().getStatus();
        if (status == Response.Status.UNAUTHORIZED.getStatusCode()) {
            return Response
                       .status(Response.Status.UNAUTHORIZED)
                       .entity(new PublicErrorResource().view401())
                       .build();
        } else if (status == Response.Status.NOT_FOUND.getStatusCode()) {
            return Response
                       .status(Response.Status.NOT_FOUND)
                       .entity(new PublicErrorResource().view404())
                       .build();
        } else {
            log.error(exception.getMessage(), exception);
            return defaultResponse;
        }
    }

}
