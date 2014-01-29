/*
 * Copyright 2014 Jeanfrancois Arcand
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
package org.atmosphere.gwt20.server;

import com.google.gwt.user.client.rpc.SerializationException;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.gwt20.shared.Constants;
import org.atmosphere.handler.ReflectorServletProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author p.havelaar
 */
public class GwtRpcInterceptor implements AtmosphereInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GwtRpcInterceptor.class);
    public static final String X_WEBSOCKET_GWT = GwtRpcInterceptor.class.getName() + ".useWebSocket";

    @Override
    public void configure(AtmosphereConfig config) {
    }

    @Override
    public Action inspect(AtmosphereResource r) {

        boolean jersey = isHandledByJersey(r);
        // All WebSocket messages needs to use the Constants.GWT_RPC_MEDIA_TYPE for content type.
        // Here we just force it
        if (!jersey && r.getRequest().getAttribute(X_WEBSOCKET_GWT) != null) {
            r.getRequest().contentType(Constants.GWT_RPC_MEDIA_TYPE);
        }

        if (r.getRequest().getContentType() == null
                || !r.getRequest().getContentType().contains("x-gwt-rpc")
                || jersey) {
            return Action.CONTINUE;
        }

        logger.debug("Found GWT-RPC Atmosphere request. method: " + r.getRequest().getMethod());

        if (r.getRequest().getMethod().equals("GET")) {
            // For default content-type.
            if (r.transport().equals(AtmosphereResource.TRANSPORT.WEBSOCKET)) {
                r.getRequest().setAttribute(X_WEBSOCKET_GWT, Boolean.TRUE);
            }

            if (!(r.getSerializer() instanceof GwtRpcSerializer)) {

                String contentType = r.getRequest().getContentType();
                String charEncoding = r.getRequest().getCharacterEncoding();
                if (charEncoding == null) {
                    charEncoding = "UTF-8";
                }
                r.getResponse().setContentType(contentType);
                r.getResponse().setCharacterEncoding(charEncoding);
                r.setSerializer(new GwtRpcSerializer(r));

            }
        } else if (r.getRequest().getMethod().equals("POST")) {
            try {
                String data = GwtRpcUtil.readerToString(r.getRequest().getReader());
                if (logger.isDebugEnabled()) {
                    logger.debug("Received message from client: " + data);
                }
                r.getRequest().setAttribute(Constants.MESSAGE_OBJECT, GwtRpcUtil.deserialize(data));
            } catch (IOException ex) {
                logger.error("Failed to read request data", ex);
            } catch (SerializationException ex) {
                logger.error("Failed to deserialize GWT RPC data");
            }
        }
        return Action.CONTINUE;
    }

    protected boolean isHandledByJersey(AtmosphereResource r) {
        if (r.getAtmosphereHandler() instanceof ReflectorServletProcessor) {
            return ReflectorServletProcessor.class.cast(r.getAtmosphereHandler()).getServletClassName()
                    .equals("com.sun.jersey.spi.container.servlet.ServletContainer");
        } else {
            return false;
        }
    }

    @Override
    public void postInspect(AtmosphereResource r) {

    }

}
