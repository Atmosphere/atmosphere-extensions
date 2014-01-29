package org.atmosphere.gwt20.client;

public interface AtmosphereSubscriber
{
    AtmosphereServerRequest subscribe(RequestConfig requestConfig);
    void unsubscribe();
}