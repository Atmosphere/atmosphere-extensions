package org.atmosphere.plugin.hazelcast;

import java.io.Serializable;
import java.util.Map;

/**
 * Simple message structure that will be added to hazelcast topic.
 *
 * @author Darrel Kleynhans
 */
public class TopicMessage implements Serializable {

    private Map<String, String> headers;
    private String broadcasterId;
    private String message;

    public TopicMessage( Map<String, String> headers, String broadcasterId, String message) {
        this.headers = headers;
        this.broadcasterId = broadcasterId;
        this.message = message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBroadcasterId() {
        return broadcasterId;
    }

    public void setBroadcasterId(String broadcasterId) {
        this.broadcasterId = broadcasterId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
