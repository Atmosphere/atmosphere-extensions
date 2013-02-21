package org.atmosphere.extensions.gwtwrapper.client;

/**
 *
 * @author jotec
 */
public interface AtmosphereMessagePublishedHandler {
    public void onMessagePublished(AtmosphereRequestConfig request, AtmosphereResponse response);
}
