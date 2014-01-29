package org.atmosphere.gwt20.client;

import com.google.gwt.http.client.RequestBuilder.Method;

public interface RequestConfig
{
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
        enableProtocol
    }

    void setFlags(Flags ... flags);

    void clearFlags(Flags ... flags);

    void setOutboundSerializer(ClientSerializer serializer);

    void setHeader(String name, String value);

    void setMaxReconnectOnClose(int maxReconnectOnClose);

    void setContentType(String contentType);

    void setUrl(String url);

    void setConnectTimeout(int connectTimeout);

    void setReconnectInterval(int reconnectInterval);

    void setTimeout(int timeout);

    void setLogLevel(String logLevel);

    void setMaxRequest(int maxRequest);

    void setMaxStreamingLength(int maxStreamingLength);

    void setMethod(Method method);

    void setFallbackMethod(Method method);

    void setTransport(Transport transport);

    void setFallbackTransport(Transport transport);

    void setOpenHandler(AtmosphereOpenHandler handler);

    void setReopenHandler(AtmosphereReopenHandler handler);

    void setCloseHandler(AtmosphereCloseHandler handler);

    void setClientTimeoutHandler(AtmosphereClientTimeoutHandler handler);

    void setMessageHandler(AtmosphereMessageHandler handler);

    void setLocalMessageHandler(AtmosphereMessageHandler handler);

    void setErrorHandler(AtmosphereErrorHandler handler);

    void setReconnectHandler(AtmosphereReconnectHandler handler);

    void setFailureToReconnectHandler(AtmosphereFailureToReconnectHandler handler);

    void setMessagePublishedHandler(AtmosphereMessagePublishedHandler handler);

    void setTransportFailureHandler(AtmosphereTransportFailureHandler handler);

    ClientSerializer getOutboundSerializer();
}