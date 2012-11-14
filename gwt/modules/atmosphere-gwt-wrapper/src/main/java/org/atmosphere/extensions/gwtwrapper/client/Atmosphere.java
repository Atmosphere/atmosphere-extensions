package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;

/**
 *
 * @author jotec
 */
public final class Atmosphere extends JavaScriptObject {
    
    public static final String STRONG_NAME_PARAMETER = "gwt_strong_name";
    public static final String MODULE_BASE_PARAMETER = "gwt_module_base";
 
    // included the javascript libraries
    static {
        include("javascript/portal-1.0rc1.js");
        include("javascript/atmosphere.js");
    }

    public static native Atmosphere create() /*-{
        return $wnd.atmosphere;
    }-*/;
    
    public AtmosphereRequest subscribe(AtmosphereRequest request) {
        request.setHeader(MODULE_BASE_PARAMETER, GWT.getModuleBaseURL());
        request.setHeader(STRONG_NAME_PARAMETER, GWT.getPermutationStrongName());
        request.setContentType("text/x-gwt-rpc");
        return subscribeImpl(request);
    }
    
    private native AtmosphereRequest subscribeImpl(AtmosphereRequest request) /*-{
        return this.subscribe(request);
    }-*/;
    
    protected Atmosphere() {
    }
    
    protected static void include(String source) {
        ScriptElement el = Document.get().createScriptElement();
        el.setType("text/javascript");
        el.setSrc(source);
        Document.get().getBody().appendChild(el);
    }

}
