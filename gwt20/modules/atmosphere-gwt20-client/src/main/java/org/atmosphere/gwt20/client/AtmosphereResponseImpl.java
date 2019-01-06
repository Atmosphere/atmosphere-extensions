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
import org.atmosphere.gwt20.client.AtmosphereRequestConfig.Transport;

import java.util.Collections;
import java.util.List;

/**
 * @author jotec
 */
public final class AtmosphereResponseImpl extends JavaScriptObject implements AtmosphereResponse {

    public enum State {
        MESSAGE_RECEIVED,
        MESSAGE_PUBLISHED,
        OPENING,
        RE_OPENING,
        CLOSED,
        UNSUBSCRIBE,
        ERROR;

        @Override
        public String toString() {
            switch (this) {
                case MESSAGE_RECEIVED:
                    return "messageReceived";
                case MESSAGE_PUBLISHED:
                    return "messagePublished";
                case OPENING:
                    return "opening";
                case RE_OPENING:
                    return "re-opening";
                case CLOSED:
                    return "closed";
                case UNSUBSCRIBE:
                    return "unsubscribe";
                default:
                case ERROR:
                    return "error";
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

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.ResponseManager#getStatus()
     */
    @Override
    public native int getStatus() /*-{
        return this.status;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.ResponseManager#getReasonPhrase()
     */
    @Override
    public native String getReasonPhrase() /*-{
        return this.reasonPhrase;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.ResponseManager#getMessages()
     */
    @Override
    public <T> List<T> getMessages() {
        Object containedMessage = getMessageObject();
        if (containedMessage == null) {
            return Collections.emptyList();
        } else if (containedMessage instanceof List) {
            return (List) containedMessage;
        } else {
            return (List<T>) Collections.singletonList(containedMessage);
        }
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.ResponseManager#getResponseBody()
     */
    @Override
    public native String getResponseBody() /*-{
        return this.responseBody;
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.ResponseManager#getHeader(java.lang.String)
     */
    @Override
    public native String getHeader(String name) /*-{
        return this.headers[name];
    }-*/;

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.ResponseManager#getState()
     */
    @Override
    public State getState() {
        return State.fromString(getStateImpl());
    }

    /* (non-Javadoc)
     * @see org.atmosphere.gwt20.client.ResponseManager#getTransport()
     */
    @Override
    public Transport getTransport() {
        return Transport.fromString(getTransportImpl());
    }

    @Override
    public native void setMessageObject(Object message) /*-{
        this.messageObject = message;
    }-*/;

    protected AtmosphereResponseImpl() {
    }

    native Object getMessageObject() /*-{
        return this.messageObject;
    }-*/;

    private native String getStateImpl() /*-{
        return this.state;
    }-*/;

    private native String getTransportImpl() /*-{
        return this.transport;
    }-*/;

}
