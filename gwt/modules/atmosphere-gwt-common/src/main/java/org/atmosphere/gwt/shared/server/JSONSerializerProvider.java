package org.atmosphere.gwt.shared.server;

/**
 *
 * @author p.havelaar
 */
public interface JSONSerializerProvider {
    
    JSONSerializer getSerializer();
    JSONDeserializer getDeserializer();
}
