package org.atmosphere.gwt20.client;

import org.atmosphere.gwt20.client.AtmosphereRequestConfig.Transport;
import org.atmosphere.gwt20.client.AtmosphereResponseImpl.State;

import java.util.List;

public interface AtmosphereResponse {
    /**
     * See com.google.gwt.http.client.Response for status codes
     *
     * @return
     */
    int getStatus();

    String getReasonPhrase();

    <T> List<T> getMessages();

    String getResponseBody();

    String getHeader(String name);

    State getState();

    Transport getTransport();

    void setMessageObject(Object inMessage);

}