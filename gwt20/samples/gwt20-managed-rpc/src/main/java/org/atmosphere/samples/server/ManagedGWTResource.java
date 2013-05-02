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

import org.atmosphere.config.service.Get;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Post;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.BroadcastFilter;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.gwt20.server.GwtRpcInterceptor;
import org.atmosphere.gwt20.shared.Constants;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.atmosphere.samples.client.RPCEvent;

import java.util.logging.Logger;

/**
 * Super simple managed echo application that use two broadcaster for pushing data back to the client.
 *
 * @author Jeanfrancois Arcand
 */
@ManagedService(path = "/GwtRpcDemo/atmosphere/rpc", interceptors = {
        /**
         * Handle lifecycle for us
         */
        AtmosphereResourceLifecycleInterceptor.class,
        /**
         * Serialize/Deserialize GWT message for us
         */
        GwtRpcInterceptor.class,
        /**
         * Make sure our {@link AtmosphereResourceEventListener#onSuspend} is only called once for transport
         * that reconnect on every requests.
         */
        SuspendTrackerInterceptor.class
})
public class ManagedGWTResource {

    static final Logger logger = Logger.getLogger("AtmosphereHandler");
    /**
     * Another {@link Broadcaster} that will be used to publish data back to the client.
     */
    public final Broadcaster connectedUsers;

    public ManagedGWTResource(){
        connectedUsers = BroadcasterFactory.getDefault().lookup("Connected users", true);
        connectedUsers.getBroadcasterConfig().addFilter(new BroadcastFilter() {
            /**
             * Wrap message inside our {@link RPCEvent}
             * @param originalMessage the String message
             * @param message the String message
             * @return an RPCEvent instance.
             */
            @Override
            public BroadcastAction filter(Object originalMessage, Object message) {
                RPCEvent e = new RPCEvent();
                e.setData("User " + message + " connected");
                return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, e);
            }
        });
    }


    @Get
    public void get(final AtmosphereResource ar) {
        /**
         * For demonstration purpose, we add callback for when the client connect and disconnect.
         */
        ar.addEventListener(new AtmosphereResourceEventListenerAdapter() {
            @Override
            public void onSuspend(AtmosphereResourceEvent event) {
                logger.info("Received RPC GET");
                connectedUsers.addAtmosphereResource(ar).broadcast(ar.uuid());
            }

            @Override
            public void onDisconnect(AtmosphereResourceEvent event) {
                // isCancelled == true. means the client didn't send the close event, so an unexpected network glitch or browser
                // crash occurred.
                if (event.isCancelled()) {
                    logger.info("User:" + event.getResource().uuid() + " unexpectedly disconnected");
                } else if (event.isClosedByClient()) {
                    logger.info("User:" + event.getResource().uuid() + " closed the connection");
                }
            }
        });
    }

    /**
     * Receive push message from the browser.
     */
    @Post
    public void doPost(AtmosphereResource ar) {
        Object msg = ar.getRequest().getAttribute(Constants.MESSAGE_OBJECT);
        logger.info("Transport used: " + ar.transport().toString());
        if (msg != null) {
            logger.info("received RPC post: " + msg.toString());
            // Here we lookup the default broadcaster, mapped to the Broadcaster called /GwtRpcDemo/atmosphere/rpc
            ar.getBroadcaster().broadcast(msg);
        }
    }
}
