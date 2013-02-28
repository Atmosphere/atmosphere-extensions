/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.SerializationException;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 *
 * @author rinchen tenpel
 */
public final class AtmosphereRequest extends JavaScriptObject {
  
  static final Logger logger = Logger.getLogger("AtmosphereRequest");
  
  public native void open() /*-{
    this.open();
  }-*/;
  
  public void push(Serializable message) throws SerializationException {
    this.pushImpl(getSerializer().serialize(message));
  }
  
  public native void pushImpl(String message) /*-{
    this.push(message);
  }-*/;
  
  public void pushLocal(Serializable message) throws SerializationException {
    this.pushLocalImpl(getSerializer().serialize(message));
  }
  
  public native void pushLocalImpl(String message) /*-{
    this.pushLocal(message);
  }-*/;
  
  protected AtmosphereRequest() {
    
  }
  
  native void setSerializer(GwtClientSerializer serializer) /*-{
    this.serializer = serializer;
  }-*/;

  native GwtClientSerializer getSerializer() /*-{
    return this.serializer;
  }-*/;
}
