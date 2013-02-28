package org.atmosphere.samples.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.logging.client.HasWidgetsLogHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.atmosphere.extensions.gwtwrapper.client.Atmosphere;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereCloseHandler;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereMessageHandler;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereOpenHandler;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereRequest;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereRequestConfig;
import org.atmosphere.extensions.gwtwrapper.client.AtmosphereRequestConfig.Flags;
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
        
        
        HorizontalPanel buttons = new HorizontalPanel();
        final TextBox messageInput = new TextBox();
        buttons.add(messageInput);
        
        Button send = new Button("send");
        buttons.add(send);
        
        Button sendLocal = new Button("send local");
        buttons.add(sendLocal);
        
        RootPanel.get("buttonbar").add(buttons);
        
        
        HTMLPanel logPanel = new HTMLPanel("") {
            @Override
            public void add(Widget widget) {
                super.add(widget);
                widget.getElement().scrollIntoView();
            }
        };
        RootPanel.get("logger").add(logPanel);
        Logger.getLogger("").addHandler(new HasWidgetsLogHandler(logPanel));
        
        Serializer serializer = GWT.create(Serializer.class);
                
        AtmosphereRequestConfig requestConfig = AtmosphereRequestConfig.create(serializer);
        requestConfig.setUrl(GWT.getModuleBaseURL() + "atmosphere");
        requestConfig.setContentType("text/x-gwt-rpc");
        requestConfig.setTransport(AtmosphereRequestConfig.Transport.STREAMING);
        requestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
        requestConfig.setOpenHandler(new AtmosphereOpenHandler() {
            @Override
            public void onOpen(AtmosphereResponse response) {
                logger.info("Connection opened");
            }
        });
        requestConfig.setCloseHandler(new AtmosphereCloseHandler() {
            @Override
            public void onClose(AtmosphereResponse response) {
                logger.info("Connection closed");
            }
        });
        requestConfig.setMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
                Event event = (Event) response.getMessageObject();
                if (event != null) {
                    logger.info(event.getData());
                }
            }
        });
        requestConfig.setLocalMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
                Event event = (Event) response.getMessageObject();
                if (event != null) {
                    logger.info("local message: " + event.getData());
                }
            }
        });
        // trackMessageLength is not required but makes the connection more robust
        requestConfig.setFlags(Flags.trackMessageLength);
        
        Atmosphere atmosphere = Atmosphere.create();
        final AtmosphereRequest request = atmosphere.subscribe(requestConfig);
        
        send.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (messageInput.getText().trim().length() > 0) {
              try {
                //              service.sendEvent(new Event(messageInput.getText()), callback);
                              request.push(new Event(messageInput.getText()));
              } catch (SerializationException ex) {
                logger.log(Level.SEVERE, "Failed to serializer message", ex);
              }
            }
          }
        });
        
        sendLocal.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (messageInput.getText().trim().length() > 0) {
              try {
                   request.pushLocal(new Event(messageInput.getText()));
              } catch (SerializationException ex) {
                logger.log(Level.SEVERE, "Failed to serializer message", ex);
              }
            }
          }
        });
        
    }

}
