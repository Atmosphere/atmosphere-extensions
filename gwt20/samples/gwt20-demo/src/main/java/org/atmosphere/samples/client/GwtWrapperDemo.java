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
package org.atmosphere.samples.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.logging.client.HasWidgetsLogHandler;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.atmosphere.extensions.gwt20.client.Atmosphere;
import org.atmosphere.extensions.gwt20.client.AtmosphereCloseHandler;
import org.atmosphere.extensions.gwt20.client.AtmosphereMessageHandler;
import org.atmosphere.extensions.gwt20.client.AtmosphereOpenHandler;
import org.atmosphere.extensions.gwt20.client.AtmosphereRequest;
import org.atmosphere.extensions.gwt20.client.AtmosphereRequestConfig;
import org.atmosphere.extensions.gwt20.client.AtmosphereRequestConfig.Flags;
import org.atmosphere.extensions.gwt20.client.AtmosphereResponse;
import org.atmosphere.extensions.gwt20.client.AutoBeanClientSerializer;

/**
 *
 * @author jotec
 */
public class GwtWrapperDemo implements EntryPoint {

    static final Logger logger = Logger.getLogger(GwtWrapperDemo.class.getName());
    
    private MyBeanFactory beanFactory = GWT.create(MyBeanFactory.class);

    @Override
    public void onModuleLoad() {
        
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                logger.log(Level.SEVERE, "Uncaught exception", e);
            }
        });
        
      
        HorizontalPanel buttons = new HorizontalPanel();
        final TextBox messageInput = new TextBox();
        buttons.add(messageInput);
        
        Button sendRPC = new Button("send (GWT-RPC)");
        buttons.add(sendRPC);
        
        Button sendJSON = new Button("send (JSON)");
        buttons.add(sendJSON);
        
                
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
        
                
        RPCSerializer rpc_serializer = GWT.create(RPCSerializer.class);
        AutoBeanClientSerializer json_serializer = new AutoBeanClientSerializer();
        json_serializer.registerBeanFactory(beanFactory, Event.class);        
                
        AtmosphereRequestConfig rpcRequestConfig = AtmosphereRequestConfig.create(rpc_serializer);
        rpcRequestConfig.setUrl(GWT.getModuleBaseURL() + "atmosphere/rpc");
        rpcRequestConfig.setTransport(AtmosphereRequestConfig.Transport.STREAMING);
        rpcRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
        rpcRequestConfig.setOpenHandler(new AtmosphereOpenHandler() {
            @Override
            public void onOpen(AtmosphereResponse response) {
                logger.info("RPC Connection opened");
            }
        });
        rpcRequestConfig.setCloseHandler(new AtmosphereCloseHandler() {
            @Override
            public void onClose(AtmosphereResponse response) {
                logger.info("RPC Connection closed");
            }
        });
        rpcRequestConfig.setMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
                RPCEvent event = (RPCEvent) response.getMessageObject();
                if (event != null) {
                    logger.info("received message through RPC: " + event.getData());
                }
            }
        });
        
        // trackMessageLength is not required but makes the connection more robust, does not seem to work with 
        // unicode characters
//        rpcRequestConfig.setFlags(Flags.trackMessageLength);
        rpcRequestConfig.clearFlags(Flags.dropAtmosphereHeaders);
        
        // setup JSON Atmosphere connection
        AtmosphereRequestConfig jsonRequestConfig = AtmosphereRequestConfig.create(json_serializer);
        jsonRequestConfig.setUrl(GWT.getModuleBaseURL() + "atmosphere/json");
        jsonRequestConfig.setContentType("application/json; charset=UTF-8");
        jsonRequestConfig.setTransport(AtmosphereRequestConfig.Transport.STREAMING);
        jsonRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
        jsonRequestConfig.setOpenHandler(new AtmosphereOpenHandler() {
            @Override
            public void onOpen(AtmosphereResponse response) {
                logger.info("JSON Connection opened");
            }
        });
        jsonRequestConfig.setCloseHandler(new AtmosphereCloseHandler() {
            @Override
            public void onClose(AtmosphereResponse response) {
                logger.info("JSON Connection closed");
            }
        });
        jsonRequestConfig.setMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
                Event event = (Event) response.getMessageObject();
                if (event != null) {
                    logger.info("received message through JSON: " + event.getData());
                }
            }
        });
        jsonRequestConfig.clearFlags(Flags.dropAtmosphereHeaders);
        
        
        Atmosphere atmosphere = Atmosphere.create();
        final AtmosphereRequest rpcRequest = atmosphere.subscribe(rpcRequestConfig);
        final AtmosphereRequest jsonRequest = atmosphere.subscribe(jsonRequestConfig);
        
        sendRPC.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (messageInput.getText().trim().length() > 0) {
              try {
                //              service.sendEvent(new Event(messageInput.getText()), callback);
                  RPCEvent myevent = new RPCEvent();
                  myevent.setData(messageInput.getText());
                  rpcRequest.push(myevent);
              } catch (SerializationException ex) {
                logger.log(Level.SEVERE, "Failed to serialize message", ex);
              }
            }
          }
        });
        
        
        sendJSON.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (messageInput.getText().trim().length() > 0) {
              try {
                //              service.sendEvent(new Event(messageInput.getText()), callback);
                  Event myevent = beanFactory.create(Event.class).as();
                  myevent.setData(messageInput.getText());
                  jsonRequest.push(myevent);
              } catch (SerializationException ex) {
                logger.log(Level.SEVERE, "Failed to serialize message", ex);
              }
            }
          }
        });
        
        
    }

}
