package org.atmosphere.extensions.gwtwrapper.server;

import java.io.BufferedReader;
import java.io.IOException;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.extensions.gwtwrapper.client.Atmosphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jotec
 */
public class GwtRpcInterceptor implements AtmosphereInterceptor {
  
  private static final Logger logger = LoggerFactory.getLogger(GwtRpcInterceptor.class);

    @Override
    public void configure(AtmosphereConfig config) {
    }

    @Override
    public Action inspect(AtmosphereResource r) {
      
        String module_base = r.getRequest().getHeader(Atmosphere.MODULE_BASE_PARAMETER);
        if (module_base == null) {
          module_base = r.getRequest().getParameter(Atmosphere.MODULE_BASE_PARAMETER);
        }
      
        logger.debug("Inspecting Atmosphere request. base: " + module_base
                + " method: " + r.getRequest().getMethod());
        
        if (module_base != null && !(r.getSerializer() instanceof GwtRpcSerializer)) {
          
            if (r.getRequest().getMethod().equals("GET")) {

                r.getResponse().setContentType(r.getRequest().getContentType());
                r.getResponse().setCharacterEncoding(r.getRequest().getCharacterEncoding());
                r.setSerializer(new GwtRpcSerializer(r));

            } else if (r.getRequest().getMethod().equals("POST")) {

                StringBuilder data = new StringBuilder();
                BufferedReader requestReader;
                try {
                    requestReader = r.getRequest().getReader();
                    char[] buf = new char[5120];
                    int read = -1;
                    while ((read = requestReader.read(buf)) > 0) {
                      data.append(buf, 0, read);
                    }
                    if (logger.isDebugEnabled()) {
                      logger.debug("Received message from client: " + data.toString());
                    }
                    r.getRequest().setAttribute(Atmosphere.MESSAGE_OBJECT, new GwtRpcSerializer(r).deserialize(data.toString()));
                } catch (IOException ex) {
                    logger.error("Failed to read request data", ex);
                }
            }
        }
        return Action.CONTINUE;
    }
    
    @Override
    public void postInspect(AtmosphereResource r) {
        
    }
    
}
