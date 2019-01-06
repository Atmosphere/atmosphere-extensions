/*
 * Copyright 2008-2019 Async-IO.org
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
package org.atmosphere.gwt20.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.SerializationException;

import java.util.logging.Logger;

/**
 * @author rinchen tenpel
 */
public final class AtmosphereRequestImpl extends JavaScriptObject implements AtmosphereRequest {

    static final Logger logger = Logger.getLogger("AtmosphereRequest");

    /* (non-Javadoc)
   * @see org.atmosphere.gwt20.client.AtmosphereMessageManager#push(java.lang.Object)
   */
    @Override
    public void push(Object message) throws SerializationException {
        this.pushImpl(getOutboundSerializer().serialize(message));
    }

    /* (non-Javadoc)
   * @see org.atmosphere.gwt20.client.AtmosphereMessageManager#pushImpl(java.lang.String)
   */
    @Override
    public native void pushImpl(String message) /*-{
        this.push(message);
    }-*/;

    /* (non-Javadoc)
   * @see org.atmosphere.gwt20.client.AtmosphereMessageManager#pushLocal(java.lang.Object)
   */
    @Override
    public void pushLocal(Object message) throws SerializationException {
        this.pushLocalImpl(getOutboundSerializer().serialize(message));
    }

    /* (non-Javadoc)
   * @see org.atmosphere.gwt20.client.AtmosphereMessageManager#pushLocalImpl(java.lang.String)
   */
    @Override
    public native void pushLocalImpl(String message) /*-{
        this.pushLocal(message);
    }-*/;

    protected AtmosphereRequestImpl() {

    }

    native void setOutboundSerializer(ClientSerializer serializer) /*-{
        this.serializer = serializer;
    }-*/;

    native ClientSerializer getOutboundSerializer() /*-{
        return this.serializer;
    }-*/;

    /* (non-Javadoc)
   * @see org.atmosphere.gwt20.client.AtmosphereMessageManager#getUUID()
   */
    @Override
    public native String getUUID() /*-{
        return String(this.getUUID());
    }-*/;
}
