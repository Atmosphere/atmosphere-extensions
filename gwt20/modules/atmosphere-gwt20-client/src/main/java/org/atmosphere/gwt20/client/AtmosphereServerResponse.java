package org.atmosphere.gwt20.client;

import java.util.List;

import org.atmosphere.gwt20.client.AtmosphereResponse.State;
import org.atmosphere.gwt20.client.RequestConfig.Transport;

public interface AtmosphereServerResponse
{
    /**
     * See com.google.gwt.http.client.Response for status codes
     *
     * @return
     */
    int getStatus();

    String getReasonPhrase() ;

    <T> List<T> getMessages();

    String getResponseBody();

    String getHeader(String name);

    State getState();

    Transport getTransport();

    void setMessageObject(Object inMessage);

}