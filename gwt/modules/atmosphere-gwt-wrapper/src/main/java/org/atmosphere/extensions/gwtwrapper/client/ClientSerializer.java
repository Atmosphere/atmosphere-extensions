/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.user.client.rpc.SerializationException;

/**
 *
 * @author p.havelaar
 */
public interface ClientSerializer {
    
    /**
     * You need to implement this method in your serializer and call the respective implementations
     * like deserializeRPC
     */
    public Object deserialize(String message) throws SerializationException;
    
    /**
     * You need to implement this method in your serializer and call the respective implementations
     * like serializeRPC
     */
    public String serialize(Object message) throws SerializationException;
    
}
