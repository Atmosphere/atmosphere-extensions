/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.extensions.gwt.jersey;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.atmosphere.gwt.shared.Constants;

/**
 *
 * @author p.havelaar
 */
@Provider
public class JerseyRPCProvider implements InjectableProvider<GwtPayload, Type> {
    
    @Context
    HttpServletRequest request;

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext cc, GwtPayload a, Type c) {
        return new Injectable<Object>() {
            @Override
            public Object getValue() {
                return request.getAttribute(Constants.MESSAGE_OBJECT);
            }
        };
    }

    
}
