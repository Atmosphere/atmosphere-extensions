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
