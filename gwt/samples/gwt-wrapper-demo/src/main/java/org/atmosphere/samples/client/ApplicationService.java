package org.atmosphere.samples.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *
 * @author jotec
 */
@RemoteServiceRelativePath("appservice")
public interface ApplicationService extends RemoteService {
    
    public void sendEvent(Event e);
}
