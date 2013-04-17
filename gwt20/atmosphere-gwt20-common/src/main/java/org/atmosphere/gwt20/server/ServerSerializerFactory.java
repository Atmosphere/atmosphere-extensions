/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.gwt20.server;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 *
 * @author Havelaar
 */
public class ServerSerializerFactory {
   
   private static ServiceLoader<ServerSerializer> loader = ServiceLoader.load(ServerSerializer.class);
   
   public static Iterator<ServerSerializer> getSerializers() {
      return loader.iterator();
   }
}
