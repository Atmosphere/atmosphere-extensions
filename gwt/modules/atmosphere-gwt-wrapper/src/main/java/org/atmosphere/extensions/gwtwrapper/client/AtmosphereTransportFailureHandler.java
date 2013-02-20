package org.atmosphere.extensions.gwtwrapper.client;

/**
 *
 * @author jotec
 */
public interface AtmosphereTransportFailureHandler {
    public void onTransportFailure(String errorMsg, AtmosphereResponse response);
}
