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
package org.atmosphere.samples.server;

import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Post;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.gwt20.server.GwtRpcInterceptor;
import org.atmosphere.gwt20.shared.Constants;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;

import java.util.logging.Logger;

/**
 * This is a simple handler example to show how to use GWT RPC serialization
 *
 * @author p.havelaar
 */
@ManagedService(path = "/GwtRpcDemo/atmosphere/rpc", interceptors = {
        AtmosphereResourceLifecycleInterceptor.class,
        GwtRpcInterceptor.class
})
public class GwtRpcAtmosphereHandler {

    static final Logger logger = Logger.getLogger("AtmosphereHandler");

    /**
     * receive push message from client
     */
    @Post
    public void doPost(AtmosphereResource ar) {
        Object msg = ar.getRequest().getAttribute(Constants.MESSAGE_OBJECT);
        if (msg != null) {
            logger.info("received RPC post: " + msg.toString());
            // for demonstration purposes we will broadcast the message to all connections
            ar.getBroadcaster().broadcast(msg);
        }
    }
}
