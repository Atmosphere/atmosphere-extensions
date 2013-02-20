package org.atmosphere.samples.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.atmosphere.extensions.gwtwrapper.client.Atmosphere;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereCloseHandler;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereMessageHandler;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereOpenHandler;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereRequest;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereResponse;

/**
 *
 * @author jotec
 */
public class GwtWrapperDemo implements EntryPoint {

    static final Logger logger = Logger.getLogger(GwtWrapperDemo.class.getName());

    @Override
    public void onModuleLoad() {
        
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                logger.log(Level.SEVERE, "Uncaught exception", e);
            }
        });
        Serializer serializer = GWT.create(Serializer.class);
        
        Atmosphere atmosphere = Atmosphere.create();
        
        AtmosphereRequest request = AtmosphereRequest.create(serializer);
        request.setUrl(GWT.getModuleBaseURL() + "atmosphere");
        request.setContentType("text/x-gwt-rpc");
        request.setTransport(AtmosphereRequest.Transport.STREAMING);
        request.setFallbackTransport(AtmosphereRequest.Transport.LONG_POLLING);
        request.setOpenHandler(new AtmosphereOpenHandler() {
            @Override
            public void onOpen(AtmosphereResponse response) {
                Window.alert("Connection opened");
            }
        });
        request.setCloseHandler(new AtmosphereCloseHandler() {
            @Override
            public void onClose(AtmosphereResponse response) {
                Window.alert("Connection closed");
            }
        });
        request.setMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
                Event event = (Event) response.getMessageObject();
                if (event != null) {
                    logger.info(event.getData());
                }
            }
        });
        atmosphere.subscribe(request);
        
        final ApplicationServiceAsync service = ApplicationServiceAsync.Util.getInstance();
        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed application RPC call", caught);
            }
            @Override
            public void onSuccess(Void result) {
            }
        };
        
        new Timer() {
            @Override
            public void run() {
                service.sendEvent(new Event(new Date().toString()), callback);
            }
        }.scheduleRepeating(1000);
    }

}
