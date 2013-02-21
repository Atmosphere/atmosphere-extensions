/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.JavaScriptObject;
import java.io.Serializable;

/**
 *
 * @author rinchen tenpel
 */
public final class AtmosphereRequest extends JavaScriptObject {
  
  public native void open() /*-{
    this.open();
  }-*/;
  
  public void push(Serializable message) {
    throw new UnsupportedOperationException("Not implemented yet");
//    this.pushImpl(message);
  }
  
  public void pushLocal(Serializable message) {
    throw new UnsupportedOperationException("Not implemented yet");
//    this.pushLocalImpl(message);
  }
  
  protected AtmosphereRequest() {
    
  }
}
