/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.gwt20.jersey;

import com.google.gwt.user.client.rpc.SerializationException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.atmosphere.gwt20.shared.Constants;
import org.atmosphere.gwt20.server.GwtRpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author p.havelaar
 */
@Provider
@Produces(Constants.GWT_RPC_MEDIA_TYPE)
public class GwtRpcWriter implements MessageBodyWriter<Object> {

    private final static Logger logger = LoggerFactory.getLogger(GwtRpcWriter.class);

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return mt.isCompatible(MediaTypes.GWT_RPC_MEDIA_TYPE);
    }

    @Override
    public long getSize(Object t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(Object t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException, WebApplicationException {
        try {
            List ct = Collections.singletonList(Constants.GWT_RPC_MEDIA_TYPE + "; charset=UTF-8");
            if (!ct.equals(headers.get("Content-Type"))) {
                headers.put("Content-Type", ct);
            }
            if (t instanceof String && ((String) t).isEmpty()) {
                out.flush();
                return;
            }
            String data = GwtRpcUtil.serialize(t);
            out.write(data.getBytes("UTF-8"));
            out.flush();
        } catch (SerializationException ex) {
            logger.error("Failed to serialize message", ex);
        }
    }

}
