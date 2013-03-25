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

/**
 *
 * @author jotec
 */
public final class Atmosphere extends JavaScriptObject {
 
    public static native Atmosphere create() /*-{
        return $wnd.jQuery.atmosphere;
    }-*/;
    
    public AtmosphereRequest subscribe(AtmosphereRequestConfig requestConfig) {
        AtmosphereRequest r = subscribeImpl(requestConfig);
        r.setOutboundSerializer(requestConfig.getOutboundSerializer());
        return r;
    }
    
    public native void unsubscribe() /*-{
      this.unsubscribe();
    }-*/;
    
    private native AtmosphereRequest subscribeImpl(AtmosphereRequestConfig requestConfig) /*-{
      return this.subscribe(requestConfig);
    }-*/;
    
    protected Atmosphere() {
    }
 
}
