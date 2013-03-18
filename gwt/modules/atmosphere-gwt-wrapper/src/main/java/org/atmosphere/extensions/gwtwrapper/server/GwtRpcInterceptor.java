/*
 * Copyright 2013 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
        
        if (!r.getRequest().getContentType().startsWith(Atmosphere.GWT_RPC_MEDIA_TYPE)) {
            return Action.CONTINUE;
        }
              
        logger.debug("Found GWT-RPC Atmosphere request. method: " + r.getRequest().getMethod());
        
        if (!(r.getSerializer() instanceof GwtRpcSerializer)) {
          
            if (r.getRequest().getMethod().equals("GET")) {

                String contentType = r.getRequest().getContentType();
                String charEncoding = r.getRequest().getCharacterEncoding();
                r.getResponse().setContentType(contentType);
                r.getResponse().setCharacterEncoding(charEncoding);
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
