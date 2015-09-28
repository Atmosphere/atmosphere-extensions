/*
 * Copyright 2015 Async-IO.org
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
package org.atmosphere.gwt20.managed;

import org.atmosphere.cpr.BroadcastFilter;
import org.atmosphere.gwt20.client.AtmosphereMessage;
import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Wrap a message into an {@link org.atmosphere.gwt20.client.AtmosphereMessage}
 *
 * @author Jeanfrancois Arcand
 */
public class AtmosphereMessageFilterEncoder implements BroadcastFilter {

    private final Logger logger = LoggerFactory.getLogger(AtmosphereMessageFilterEncoder.class);
    private Class<? extends AtmosphereMessage<?>> messageClazz = RPCEvent.class;

    /**
     * Wrap message inside our {@link RPCEvent}
     *
     * @param originalMessage the String message
     * @param message         the String message
     * @return an RPCEvent instance.
     */
    @Override
    public BroadcastAction filter(String broadcasterId, Object originalMessage, Object message) {

        if (message == null) {
            logger.warn("Message is null", message);
            return new BroadcastAction(BroadcastAction.ACTION.ABORT, message);
        }

        if (!AtmosphereMessage.class.isAssignableFrom(message.getClass())) {
            try {
                AtmosphereMessage<Object> m = (AtmosphereMessage<Object>) messageClazz.newInstance();

                if (Callable.class.isAssignableFrom(message.getClass())) {
                    message = Callable.class.cast(message).call();
                }

                if (RPCEvent.class.isAssignableFrom(message.getClass())) {
                    m.setMessage(RPCEvent.class.cast(message).getMessage());
                } else {
                    m.setMessage(message);
                }

                return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, m);
            } catch (Exception e) {
                logger.warn("Oups. Make sure your RPCEvent implements AtmosphereMessage<Your Class>. Or remove the AtmosphereMessageInterceptor {}", e);
                return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
            }
        } else {
            return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
        }
    }

    public AtmosphereMessageFilterEncoder classToEncode(Class<? extends AtmosphereMessage<?>> messageClazz) {
        this.messageClazz = messageClazz;
        return this;
    }
}
