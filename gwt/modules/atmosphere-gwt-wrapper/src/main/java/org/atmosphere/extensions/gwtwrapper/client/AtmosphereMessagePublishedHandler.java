package org.atmosphere.extensions.gwtwrapper.client;

/**
 *
 * @author jotec
 */
public interface AtmosphereMessagePublishedHandler {
    public void onMessagePublished(AtmosphereRequest request, AtmosphereResponse response);
}
