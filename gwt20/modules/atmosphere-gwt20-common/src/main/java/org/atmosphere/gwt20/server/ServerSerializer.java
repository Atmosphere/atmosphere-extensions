/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.gwt20.server;

/**
 * @author Havelaar
 */
public interface ServerSerializer {

    /**
     * You need to implement this method in your serializer
     */
    public Object deserialize(String message) throws SerializationException;

    /**
     * You need to implement this method in your serializer
     */
    public String serialize(Object message) throws SerializationException;

}
