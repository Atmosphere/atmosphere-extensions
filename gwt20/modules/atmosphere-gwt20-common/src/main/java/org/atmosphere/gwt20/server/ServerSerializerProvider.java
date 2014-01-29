/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.gwt20.server;

/**
 * @author Havelaar
 */
public interface ServerSerializerProvider {
    ServerSerializer getServerSerializer();

    String getName();
}
