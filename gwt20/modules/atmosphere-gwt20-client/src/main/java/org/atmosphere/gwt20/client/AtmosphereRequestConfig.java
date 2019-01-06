/*
 * Copyright 2008-2019 Async-IO.org
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
public final class AtmosphereRequestConfig extends JavaScriptObject implements RequestConfig {

    private static final Logger logger = Logger.getLogger(AtmosphereRequestConfig.class.getName());

    public enum Transport {
        SESSION,
        LONG_POLLING,
        STREAMING,
        JSONP,
        SSE,
        WEBSOCKET,
        NONE;

        @Override
        public String toString() {
            switch (this) {
                default:
                case SESSION:
                    return "session";
                case LONG_POLLING:
                    return "long-polling";
                case STREAMING:
                    return "streaming";
                case JSONP:
                    return "jsonp";
                case SSE:
                    return "sse";
                case WEBSOCKET:
                    return "websocket";
                case NONE:
                    return "none";
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
    }

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
        enableProtocol,
        reconnectOnServerError
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

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setFlags(org.atmosphere.gwt20.client.AtmosphereRequestConfig.Flags)
     */
    @Override
    public void setFlags(Flags... flags) {
      for (Flags f :flags) {
        setFlagImpl(f.name(), true);
      }
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#clearFlags(org.atmosphere.gwt20.client.AtmosphereRequestConfig.Flags)
     */
    @Override
    public void clearFlags(Flags... flags) {
      for (Flags f :flags) {
        setFlagImpl(f.name(), false);
      }
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public native void setHeader(String name, String value) /*-{
       if (typeof this.headers == 'undefined') {
         this.headers = {};
       }
        this.headers[name] = value;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setMaxReconnectOnClose(int)
     */
    @Override
    public native void setMaxReconnectOnClose(int maxReconnectOnClose) /*-{
        this.maxReconnectOnClose = maxReconnectOnClose;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setContentType(java.lang.String)
     */
    @Override
    public native void setContentType(String contentType) /*-{
        this.contentType = contentType;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setUrl(java.lang.String)
     */
    @Override
    public native void setUrl(String url) /*-{
        this.url = url;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setConnectTimeout(int)
     */
    @Override
    public native void setConnectTimeout(int connectTimeout) /*-{
        this.connectTimeout = connectTimeout;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setReconnectInterval(int)
     */
    @Override
    public native void setReconnectInterval(int reconnectInterval) /*-{
        this.reconnectInterval = reconnectInterval;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setTimeout(int)
     */
    @Override
    public native void setTimeout(int timeout) /*-{
        this.timeout = timeout;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setLogLevel(java.lang.String)
     */
    @Override
    public native void setLogLevel(String logLevel) /*-{
        this.logLevel = logLevel;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setMaxRequest(int)
     */
    @Override
    public native void setMaxRequest(int maxRequest) /*-{
        this.maxRequest = maxRequest;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setMaxStreamingLength(int)
     */
    @Override
    public native void setMaxStreamingLength(int maxStreamingLength) /*-{
        this.maxStreamingLength = maxStreamingLength;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setMethod(com.google.gwt.http.client.RequestBuilder.Method)
     */
    @Override
    public void setMethod(Method method) {
        setMethodImpl(method.toString());
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setFallbackMethod(com.google.gwt.http.client.RequestBuilder.Method)
     */
    @Override
    public void setFallbackMethod(Method method) {
        setFallbackMethodImpl(method.toString());
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setTransport(org.atmosphere.gwt20.client.AtmosphereRequestConfig.Transport)
     */
    @Override
    public void setTransport(Transport transport) {
        setTransportImpl(transport.toString());
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setFallbackTransport(org.atmosphere.gwt20.client.AtmosphereRequestConfig.Transport)
     */
    @Override
    public void setFallbackTransport(Transport transport) {
        setFallbackTransportImpl(transport.toString());
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setOpenHandler(org.atmosphere.gwt20.client.AtmosphereOpenHandler)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setReopenHandler(org.atmosphere.gwt20.client.AtmosphereReopenHandler)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setCloseHandler(org.atmosphere.gwt20.client.AtmosphereCloseHandler)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setClientTimeoutHandler(org.atmosphere.gwt20.client.AtmosphereClientTimeoutHandler)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setMessageHandler(org.atmosphere.gwt20.client.AtmosphereMessageHandler)
     */
    @Override
    public void setMessageHandler(AtmosphereMessageHandler handler) {
        getMessageHandlerWrapper().messageHandler = handler;
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setLocalMessageHandler(org.atmosphere.gwt20.client.AtmosphereMessageHandler)
     */
    @Override
    public void setLocalMessageHandler(AtmosphereMessageHandler handler) {
        getLocalMessageHandlerWrapper().messageHandler = handler;
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setErrorHandler(org.atmosphere.gwt20.client.AtmosphereErrorHandler)
     */
    @Override
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

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setReconnectHandler(org.atmosphere.gwt20.client.AtmosphereReconnectHandler)
     */
    @Override
    public native void setReconnectHandler(AtmosphereReconnectHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onReconnect = $entry(function(request, response) {
                handler.@org.atmosphere.gwt20.client.AtmosphereReconnectHandler::onReconnect(Lorg/atmosphere/gwt20/client/RequestConfig;Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onReconnect = null;
        }
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setFailureToReconnectHandler(org.atmosphere.gwt20.client.AtmosphereFailureToReconnectHandler)
     */
    @Override
    public native void setFailureToReconnectHandler(AtmosphereFailureToReconnectHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onFailureToReconnect = $entry(function(request, response) {
                handler.@org.atmosphere.gwt20.client.AtmosphereFailureToReconnectHandler::onFailureToReconnect(Lorg/atmosphere/gwt20/client/RequestConfig;Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onFailureToReconnect = null;
        }
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setMessagePublishedHandler(org.atmosphere.gwt20.client.AtmosphereMessagePublishedHandler)
     */
    @Override
    public native void setMessagePublishedHandler(AtmosphereMessagePublishedHandler handler) /*-{
        var self = this;
        if (handler != null) {
            this.onMessagePublished = $entry(function(request, response) {
                handler.@org.atmosphere.gwt20.client.AtmosphereMessagePublishedHandler::onMessagePublished(Lorg/atmosphere/gwt20/client/RequestConfig;Lorg/atmosphere/gwt20/client/AtmosphereResponse;)(request, response);
            });
        } else {
            this.onMessagePublished = null;
        }
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.RequestConfig#setTransportFailureHandler(org.atmosphere.gwt20.client.AtmosphereTransportFailureHandler)
     */
    @Override
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

    @Override
    public native void setOutboundSerializer(ClientSerializer serializer) /*-{
      this.serializer = serializer;
    }-*/;

    @Override
    public native ClientSerializer getOutboundSerializer() /*-{
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