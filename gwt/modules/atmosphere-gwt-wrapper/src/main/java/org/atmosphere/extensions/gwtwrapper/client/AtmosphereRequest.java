package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.user.client.rpc.SerializationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jotec
 */
public final class AtmosphereRequest extends JavaScriptObject {
    
    private static final Logger logger = Logger.getLogger(AtmosphereRequest.class.getName());

    public enum Transport {
        POLLING,
        LONG_POLLING, 
        STREAMING, 
        JSONP, 
        SSE, 
        WEBSOCKET;
        
        @Override
        public String toString() {
            switch(this) {
                default:
                case POLLING: return "polling";
                case LONG_POLLING: return "long-polling";
                case STREAMING : return "streaming"; 
                case JSONP: return "jsonp";
                case SSE: return "sse";
                case WEBSOCKET: return "websocket";
            }
        }
    };
    
    public static AtmosphereRequest create(GwtClientSerializer serializer) {
        AtmosphereRequest r = createImpl();
        AtmosphereRequestWrapper w = r.new AtmosphereRequestWrapper(serializer);
        r.setRequestWrapper(w);
        r.setMessageHandlerImpl(w);
        return r;
    }
    
    private static native AtmosphereRequest createImpl() /*-{
        return new $wnd.atmosphere.AtmosphereRequest();
    }-*/;
    
    private class AtmosphereRequestWrapper implements AtmosphereMessageHandler {

        GwtClientSerializer serializer;
        AtmosphereMessageHandler messageHandler;
        
        public AtmosphereRequestWrapper(GwtClientSerializer serializer) {
            this.serializer = serializer;
        }
       
        @Override
        public void onMessage(AtmosphereResponse response) {
            try {
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
    
    private native void setRequestWrapper(AtmosphereRequestWrapper wrapper) /*-{
        this.requestWrapper = wrapper;
    }-*/;
    
    private native AtmosphereRequestWrapper getRequestWrapper() /*-{
        return this.requestWrapper;
    }-*/;
    
    
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
    
    private native void setMethodImpl(String method) /*-{
      this.method = method;
    }-*/;
    
    public void setFallbackMethod(Method method) {
        setFallbackMethodImpl(method.toString());
    }
    
    private native void setFallbackMethodImpl(String method) /*-{
       this.fallbackMethod = method;
    }-*/;

    public void setTransport(Transport transport) {
        setTransportImpl(transport.toString());
    }
    
    public native void setTransportImpl(String transport) /*-{
      this.transport = transport;
    }-*/;
    
    public void setFallbackTransport(Transport transport) {
        setFallbackTransportImpl(transport.toString());
    }
    
    public native void setFallbackTransportImpl(String transport) /*-{
      this.fallbackTransport = transport;
    }-*/;
    
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
        getRequestWrapper().messageHandler = handler;
    }
    
    private native void setMessageHandlerImpl(AtmosphereMessageHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onMessage = $entry(function(response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereMessageHandler::onMessage(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(response);
            });
        } else {
            this.onMessage = null;
        }
    }-*/;
    
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
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereReconnectHandler::onReconnect(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereRequest;Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onReconnect = null;
        }
    }-*/;
    
    public native void setMessagePublishedHandler(AtmosphereMessagePublishedHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onMessagePublished = $entry(function(request, response) {
                handler.@org.atmosphere.extensions.gwtwrapper.client.AtmosphereMessagePublishedHandler::onMessagePublished(Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereRequest;Lorg/atmosphere/extensions/gwtwrapper/client/AtmosphereResponse;)(request, response);
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
    
    
    protected AtmosphereRequest() {
    }
    
}
