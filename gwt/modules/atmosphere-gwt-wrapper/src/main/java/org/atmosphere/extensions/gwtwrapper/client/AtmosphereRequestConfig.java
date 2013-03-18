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
package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.user.client.rpc.SerializationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jotec
 */
public final class AtmosphereRequestConfig extends JavaScriptObject {
    
    private static final Logger logger = Logger.getLogger(AtmosphereRequestConfig.class.getName());

    public enum Transport {
        SESSION,
        LONG_POLLING, 
        STREAMING, 
        JSONP, 
        SSE, 
        WEBSOCKET;
        
        @Override
        public String toString() {
            switch(this) {
                default:
                case SESSION: return "session";
                case LONG_POLLING: return "long-polling";
                case STREAMING : return "streaming"; 
                case JSONP: return "jsonp";
                case SSE: return "sse";
                case WEBSOCKET: return "websocket";
            }
        }
    };
    
    public enum Flags {
      enableXDR,
			rewriteURL,
			attachHeadersAsQueryString,
			withCredentials,
			trackMessageLength,
			shared,
			readResponsesHeaders,
			dropAtmosphereHeaders,
			executeCallbackBeforeReconnect
    }
    
    /**
     * use the same serializer for inbound and outbound
     * @param serializer
     * @return 
     */
    public static AtmosphereRequestConfig create(GwtClientSerializer serializer) {
        return create(serializer, serializer);
    }
    
    /**
     * specify a different serializer for inbound and outbound
     * 
     * @param inbound
     * @param outbound
     * @return 
     */
    public static AtmosphereRequestConfig create(GwtClientSerializer inbound, GwtClientSerializer outbound) {
        AtmosphereRequestConfig r = createImpl();
        MessageHandlerWrapper w = new MessageHandlerWrapper(inbound);
        r.setMessageHandlerImpl(w);
        w = new MessageHandlerWrapper(inbound);
        r.setLocalMessageHandlerImpl(w);
        r.setContentType(Atmosphere.GWT_RPC_MEDIA_TYPE + "; charset=UTF8");
        r.setOutboundSerializer(outbound);
        return r;
    }
    
    public void setFlags(Flags... flags) {
      for (Flags f :flags) {
        setFlagImpl(f.name(), true);
      }
    }
    
    public void clearFlags(Flags... flags) {
      for (Flags f :flags) {
        setFlagImpl(f.name(), false);
      }
    }
    
    public native void setHeader(String name, String value) /*-{
       if (typeof this.headers == 'undefined') {
         this.headers = {};
       }
        this.headers[name] = value;
    }-*/;

    public native void setContentType(String contentType) /*-{
        this.contentType = contentType;
    }-*/;
    
    public native void setUrl(String url) /*-{
        this.url = url;
    }-*/;

    public native void setConnectTimeout(int connectTimeout) /*-{
        this.connectTimeout = connectTimeout;
    }-*/;

    public native void setReconnectInterval(int reconnectInterval) /*-{
        this.reconnectInterval = reconnectInterval;
    }-*/;

    public native void setTimeout(int timeout) /*-{
        this.timeout = timeout;
    }-*/;

    public void setMethod(Method method) {
        setMethodImpl(method.toString());
    }
    
    public void setFallbackMethod(Method method) {
        setFallbackMethodImpl(method.toString());
    }
    
    public void setTransport(Transport transport) {
        setTransportImpl(transport.toString());
    }
    
    public void setFallbackTransport(Transport transport) {
        setFallbackTransportImpl(transport.toString());
    }
    
    public native void setOpenHandler(AtmosphereOpenHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onOpen = $entry(function(response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereOpenHandler::onOpen(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onOpen = null;
        }
    }-*/;
    
    public native void setCloseHandler(AtmosphereCloseHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onClose = $entry(function(response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereCloseHandler::onClose(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onClose = null;
        }
    }-*/;
    
    public void setMessageHandler(AtmosphereMessageHandler handler) {
        getMessageHandlerWrapper().messageHandler = handler;
    }
    
    public void setLocalMessageHandler(AtmosphereMessageHandler handler) {
        getLocalMessageHandlerWrapper().messageHandler = handler;
    }
    
    public native void setErrorHandler(AtmosphereErrorHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onError = $entry(function(response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereErrorHandler::onError(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onError = null;
        }
    }-*/;
    
    public native void setReconnectHandler(AtmosphereReconnectHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onReconnect = $entry(function(request, response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereReconnectHandler::onReconnect(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereRequestConfig;Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onReconnect = null;
        }
    }-*/;
    
    public native void setMessagePublishedHandler(AtmosphereMessagePublishedHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onMessagePublished = $entry(function(request, response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereMessagePublishedHandler::onMessagePublished(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereRequestConfig;Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onMessagePublished = null;
        }
    }-*/;
    
    public native void setTransportFailureHandler(AtmosphereTransportFailureHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onTransportFailure = $entry(function(errorMsg, response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereTransportFailureHandler::onTransportFailure(Ljava/lang/String;Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(errorMsg, response);
            });
        } else {
            this.onTransportFailure = null;
        }
    }-*/;
    
    native void setOutboundSerializer(GwtClientSerializer serializer) /*-{
      this.serializer = serializer;
    }-*/;

    native GwtClientSerializer getOutboundSerializer() /*-{
      return this.serializer;
    }-*/;
    
    
    protected AtmosphereRequestConfig() {
    }
    
    private static AtmosphereRequestConfig createImpl() {
      return (AtmosphereRequestConfig) JavaScriptObject.createObject();
    }
    
    private native void setMethodImpl(String method) /*-{
      this.method = method;
    }-*/;
  
    static class MessageHandlerWrapper implements AtmosphereMessageHandler {

        GwtClientSerializer serializer;
        AtmosphereMessageHandler messageHandler;
        
        public MessageHandlerWrapper(GwtClientSerializer serializer) {
            this.serializer = serializer;
        }
       
        @Override
        public void onMessage(AtmosphereResponse response) {
            try {
                if (response.getResponseBody().trim().length() == 0) {
                  return;
                }
                Object message = serializer.deserialize(response.getResponseBody());
                response.setMessageObject(message);
                if (messageHandler != null) {
                    messageHandler.onMessage(response);
                }
            } catch (SerializationException ex) {
                logger.log(Level.SEVERE, "Failed to deserialize message", ex);
            }
        }
    }
    
    native MessageHandlerWrapper getMessageHandlerWrapper() /*-{
        return this.messageHandler;
    }-*/;
    
    native MessageHandlerWrapper getLocalMessageHandlerWrapper() /*-{
        return this.localMessageHandler;
    }-*/;
    
    private native void setTransportImpl(String transport) /*-{
      this.transport = transport;
    }-*/;
    
    private native void setFallbackTransportImpl(String transport) /*-{
      this.fallbackTransport = transport;
    }-*/;
        
    private native void setMessageHandlerImpl(AtmosphereMessageHandler handler) /*-{
        var self = this;
        this.messageHandler = handler;
        if (handler != null) {
            this.onMessage = $entry(function(response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereMessageHandler::onMessage(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onMessage = null;
        }
    }-*/;
  
    private native void setLocalMessageHandlerImpl(AtmosphereMessageHandler handler) /*-{
        var self = this;
        this.localMessageHandler = handler;
        if (handler != null) {
            this.onLocalMessage = $entry(function(response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereMessageHandler::onMessage(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onLocalMessage = null;
        }
    }-*/;

    private native void setFallbackMethodImpl(String method) /*-{
       this.fallbackMethod = method;
    }-*/;

    private native void setFlagImpl(String flagname, boolean value) /*-{
      this[flagname] = value;
    }-*/;
}
