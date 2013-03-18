/*
 * Copyright 2013 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.SerializationException;
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
  
  public void push(Object message) throws SerializationException {
    this.pushImpl(getOutboundSerializer().serialize(message));
  }
  
  public native void pushImpl(String message) /*-{
    this.push(message);
  }-*/;
  
  public void pushLocal(Object message) throws SerializationException {
    this.pushLocalImpl(getOutboundSerializer().serialize(message));
  }
  
  public native void pushLocalImpl(String message) /*-{
    this.pushLocal(message);
  }-*/;
  
  protected AtmosphereRequest() {
    
  }
  
  native void setOutboundSerializer(GwtClientSerializer serializer) /*-{
    this.serializer = serializer;
  }-*/;

  native GwtClientSerializer getOutboundSerializer() /*-{
    return this.serializer;
  }-*/;
}
