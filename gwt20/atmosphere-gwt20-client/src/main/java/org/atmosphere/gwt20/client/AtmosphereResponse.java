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
package org.atmosphere.gwt20.client;

import com.google.gwt.core.client.JavaScriptObject;

import org.atmosphere.gwt20.client.AtmosphereRequestConfig.Transport;

/**
 *
 * @author jotec
 */
public final class AtmosphereResponse extends JavaScriptObject {

    public enum State {
        MESSAGE_RECEIVED,
        MESSAGE_PUBLISHED,
        OPENING,
        RE_OPENING,
        CLOSED,
        ERROR;
        
        @Override
        public String toString() {
            switch(this) {
                case MESSAGE_RECEIVED: return "messageReceived";
                case MESSAGE_PUBLISHED: return "messagePublished";
                case OPENING: return "opening";
                case RE_OPENING: return "re-opening";
                case CLOSED: return "closed";
                default:
                case ERROR: return "error";
            }
        }
        public static State fromString(String s) {
            for (State st : State.values()) {
                if (st.toString().equals(s)) {
                    return st;
                }
            }
            return State.ERROR;
        }
    }
    /**
     * See com.google.gwt.http.client.Response for status codes
     * 
     * @return 
     */
    public native int getStatus() /*-{
       return this.status;
    }-*/;
    
    public native String getResponseBody() /*-{
        return this.responseBody;
    }-*/;
    
    public native String getHeader(String name) /*-{
        return this.headers[name];
    }-*/;
    
    public native void setMessageObject(Object message) /*-{
        this.messageObject = message;
    }-*/;
    
    public native Object getMessageObject() /*-{
        return this.messageObject;
    }-*/;
    
    public State getState() {
        return State.fromString(getStateImpl());
    }
    
    private native String getStateImpl() /*-{
        return this.state;
    }-*/;
    

    public Transport getTransport() {
        return Transport.fromString(getTransportImpl());
    }

    private native String getTransportImpl() /*-{
        return this.transport;
    }-*/;

    protected AtmosphereResponse() {
    }
}
