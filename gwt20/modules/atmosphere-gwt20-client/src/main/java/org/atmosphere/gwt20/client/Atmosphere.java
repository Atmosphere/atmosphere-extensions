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

/**
 * @author jotec
 */
public final class Atmosphere extends JavaScriptObject implements AtmosphereSubscriber {

    public static AtmosphereSubscriber createAtmosphereSubscriber() {
        return create();
    }

    public static native Atmosphere create() /*-{
        return $wnd.atmosphere;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.AtmosphereSubscriber#subscribe(org.atmosphere.gwt20.client.AtmosphereRequestConfig)
     */
    @Override
    public AtmosphereRequest subscribe(RequestConfig requestConfig) {
        AtmosphereRequestImpl r = subscribeImpl(requestConfig);
        r.setOutboundSerializer(requestConfig.getOutboundSerializer());
        return r;
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.AtmosphereSubscriber#unsubscribe()
     */
    @Override
    public native void unsubscribe() /*-{
        this.unsubscribe();
    }-*/;

    private native AtmosphereRequestImpl subscribeImpl(RequestConfig requestConfig) /*-{
        return this.subscribe(requestConfig);
    }-*/;

    protected Atmosphere() {
    }

	/* (non-Javadoc)
	 * @see org.atmosphere.gwt20.client.AtmosphereSubscriber#unsubscribeUrl(java.lang.String)
	 */
	@Override
	public native void unsubscribeUrl(String url)/*-{
    	this.unsubscribeUrl();
	}-*/;
}
