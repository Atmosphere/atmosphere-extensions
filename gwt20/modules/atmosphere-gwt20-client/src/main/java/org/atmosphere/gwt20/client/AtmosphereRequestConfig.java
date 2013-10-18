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
package org.atmosphere.gwt20.client;

import org.atmosphere.gwt20.shared.Constants;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.user.client.rpc.SerializationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author p.havelaar
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

        public static Transport fromString(String s) {
            for (Transport t : Transport.values()) {
                if (t.toString().equals(s)) {
                    return t;
                }
            }
            return null;
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
        executeCallbackBeforeReconnect,
        enableProtocol
    }
    
    /**
     * use the same serializer for inbound and outbound
     * @param serializer
     * @return 
     */
    public static AtmosphereRequestConfig create(ClientSerializer serializer) {
        return create(serializer, serializer);
    }
    
    /**
     * specify a different serializer for inbound and outbound
     * 
     * @param inbound
     * @param outbound
     * @return 
     */
    public static AtmosphereRequestConfig create(ClientSerializer inbound, ClientSerializer outbound) {
        AtmosphereRequestConfig r = createImpl();
        MessageHandlerWrapper w = new MessageHandlerWrapper(inbound);
        r.setMessageHandlerImpl(w);
        w = new MessageHandlerWrapper(inbound);
        r.setLocalMessageHandlerImpl(w);
        r.setContentType(Constants.GWT_RPC_MEDIA_TYPE + "; charset=UTF-8");
        r.clearFlags(Flags.dropAtmosphereHeaders);
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

    public native void setMaxReconnectOnClose(int maxReconnectOnClose) /*-{
        this.maxReconnectOnClose = maxReconnectOnClose;
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

	public native void setLogLevel(String logLevel) /*-{
	    this.logLevel = logLevel;
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
                handler.@org.atmosphere.gwt20.client.AtmosphereOpenHandler::onOpen(Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onOpen = null;
        }
    }-*/;
    
    public native void setReopenHandler(AtmosphereReopenHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onReopen = $entry(function(response) {
                handler.@org.atmosphere.gwt20.client.AtmosphereReopenHandler::onReopen(Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onReopen = null;
        }
    }-*/;
    
    public native void setCloseHandler(AtmosphereCloseHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onClose = $entry(function(response) {
                handler.@org.atmosphere.gwt20.client.AtmosphereCloseHandler::onClose(Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onClose = null;
        }
    }-*/;

    public native void setClientTimeoutHandler(AtmosphereClientTimeoutHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onClientTimeout = $entry(function(request) {
                handler.@org.atmosphere.gwt20.client.AtmosphereClientTimeoutHandler::onClientTimeout(Lorg/atmosphere/gwt20/client/AtmosphereRequest;)(request);
            });
        } else {
            this.onClientTimeout = null;
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
                handler.@org.atmosphere.gwt20.client.AtmosphereErrorHandler::onError(Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onError = null;
        }
    }-*/;
    
    public native void setReconnectHandler(AtmosphereReconnectHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onReconnect = $entry(function(request, response) {
                handler.@org.atmosphere.gwt20.client.AtmosphereReconnectHandler::onReconnect(Lorg/atmosphere/gwt20/client/AtmosphereRequestConfig;Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onReconnect = null;
        }
    }-*/;
    
    public native void setMessagePublishedHandler(AtmosphereMessagePublishedHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onMessagePublished = $entry(function(request, response) {
                handler.@org.atmosphere.gwt20.client.AtmosphereMessagePublishedHandler::onMessagePublished(Lorg/atmosphere/gwt20/client/AtmosphereRequestConfig;Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onMessagePublished = null;
        }
    }-*/;
    
    public native void setTransportFailureHandler(AtmosphereTransportFailureHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onTransportFailure = $entry(function(errorMsg, request) {
                handler.@org.atmosphere.gwt20.client.AtmosphereTransportFailureHandler::onTransportFailure(Ljava/lang/String;Lorg/atmosphere/gwt20/client/AtmosphereRequest;)(errorMsg, request);
            });
        } else {
            this.onTransportFailure = null;
        }
    }-*/;
    
    native void setOutboundSerializer(ClientSerializer serializer) /*-{
      this.serializer = serializer;
    }-*/;

    native ClientSerializer getOutboundSerializer() /*-{
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

        ClientSerializer serializer;
        AtmosphereMessageHandler messageHandler;
        
        public MessageHandlerWrapper(ClientSerializer serializer) {
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
                logger.log(Level.SEVERE, "Failed to deserialize message: " + response.getResponseBody(), ex);
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
                handler.@org.atmosphere.gwt20.client.AtmosphereMessageHandler::onMessage(Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(response);
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
                handler.@org.atmosphere.gwt20.client.AtmosphereMessageHandler::onMessage(Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(response);
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
