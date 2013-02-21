package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 *
 * @author jotec
 */
public final class Atmosphere extends JavaScriptObject {
    
    public static final String STRONG_NAME_PARAMETER = "gwt_strong_name";
    public static final String MODULE_BASE_PARAMETER = "gwt_module_base";
 
    public static native Atmosphere create() /*-{
        return $wnd.atmosphere;
    }-*/;
    
    public AtmosphereRequest subscribe(AtmosphereRequestConfig requestConfig) {
        requestConfig.setHeader(MODULE_BASE_PARAMETER, GWT.getModuleBaseURL());
        requestConfig.setHeader(STRONG_NAME_PARAMETER, GWT.getPermutationStrongName());
        requestConfig.setContentType("text/x-gwt-rpc");
        return subscribeImpl(requestConfig);
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
