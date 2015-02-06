package org.atmosphere.gwt20.client;

public interface AtmosphereSubscriber {
    AtmosphereRequest subscribe(RequestConfig requestConfig);

    void unsubscribe();
    
    void unsubscribeUrl(String url);
}