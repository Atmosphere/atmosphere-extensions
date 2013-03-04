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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.samples.client.ApplicationService;
import org.atmosphere.samples.client.Event;

/**
 *
 * @author jotec
 */
public class ApplicationServlet extends RemoteServiceServlet
    implements ApplicationService {

    @Override
    public void sendEvent(Event e) {
        for (Broadcaster b : BroadcasterFactory.getDefault().lookupAll()) {
            b.broadcast(e);
        }
    }

}
