package org.atmosphere.gwt.shared.server;

/**
 *
 * @author p.havelaar
 */
public interface JSONDeserializer {
    Object deserialize(String data) throws SerializationException;
}
