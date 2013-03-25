/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.samples.server;

import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import org.atmosphere.annotation.Broadcast;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.annotation.Suspend;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.extensions.gwt.jersey.GwtPayload;
import org.atmosphere.gwt.shared.Constants;
import org.atmosphere.jersey.Broadcastable;
import org.atmosphere.jersey.JerseyBroadcaster;
import org.atmosphere.samples.client.RPCEvent;

/**
 * Experimental, not functioning yet
 * 
 * @author p.havelaar
 */
@Path("/jersey/rpc")
@Produces(Constants.GWT_RPC_MEDIA_TYPE)
@Consumes(Constants.GWT_RPC_MEDIA_TYPE)
public class JerseyGwtRpc {
    
    private final static Logger logger = Logger.getLogger(JerseyGwtRpc.class.getName());
    
    @GET    
    @Suspend
    public Broadcastable gwtRpcHandler(@Context AtmosphereResource ar) {
        logger.info("Suspending Jersey GWT RPC request");
        ar.setBroadcaster(
            BroadcasterFactory.getDefault().lookup(JerseyBroadcaster.class, "jersey-rpc", true)
        );
        return new Broadcastable(ar.getBroadcaster());
    }
    
    @POST
    @Broadcast
    public Broadcastable gwtRpcHandler(@GwtPayload RPCEvent event, @Context Broadcaster b) {
        logger.info("Received RPC event on Jersey: " + event.getData());
        return new Broadcastable(event, b);
    }
    
    
}
