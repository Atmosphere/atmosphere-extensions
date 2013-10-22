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
package org.atmosphere.gwt20.managed;

import org.atmosphere.cpr.BroadcastFilter;
import org.atmosphere.gwt20.client.managed.RPCEvent;

/**
 * Wrap a message into an {@link RPCEvent}
 *
 * @author Jeanfrancois Arcand
 */
public class RPCEventFilter implements BroadcastFilter {
    /**
     * Wrap message inside our {@link RPCEvent}
     *
     * @param originalMessage the String message
     * @param message         the String message
     * @return an RPCEvent instance.
     */
    @Override
    public BroadcastAction filter(Object originalMessage, Object message) {
        if (!RPCEvent.class.isAssignableFrom(message.getClass())) {
            return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, new RPCEvent(message.toString()));
        } else {
            return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
        }
    }
}
