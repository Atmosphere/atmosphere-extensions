package org.atmosphere.extensions.gwtwrapper.server;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.extensions.gwtwrapper.client.Atmosphere;

/**
 *
 * @author jotec
 */
public class GwtRpcInterceptor implements AtmosphereInterceptor {

    @Override
    public void configure(AtmosphereConfig config) {
    }

    @Override
    public Action inspect(AtmosphereResource r) {
        if (r.getRequest().getParameter(Atmosphere.MODULE_BASE_PARAMETER) != null
                && !(r.getSerializer() instanceof GwtRpcSerializer)) {
            r.setSerializer(new GwtRpcSerializer(r));
            r.getResponse().setContentType("text/x-gwt-rpc");
        }
        return Action.CONTINUE;
    }
    
    @Override
    public void postInspect(AtmosphereResource r) {
        
    }
    
}
