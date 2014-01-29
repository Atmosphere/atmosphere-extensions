package org.atmosphere.gwt20.client;

import com.google.gwt.user.client.rpc.SerializationException;

public interface AtmosphereRequest {
    void push(Object message) throws SerializationException;

    void pushImpl(String message);

    void pushLocal(Object message) throws SerializationException;

    void pushLocalImpl(String message);

    String getUUID();
}