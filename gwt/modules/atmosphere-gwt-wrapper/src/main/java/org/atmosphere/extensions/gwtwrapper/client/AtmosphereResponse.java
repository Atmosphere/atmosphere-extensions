package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.JavaScriptObject;

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
    
    protected AtmosphereResponse() {
    }
}
