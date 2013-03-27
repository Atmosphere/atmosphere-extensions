/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.extensions.gwt.jersey;

import com.google.gwt.user.client.rpc.SerializationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.atmosphere.gwt.shared.server.GwtRpcUtil;
import org.atmosphere.gwt.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author p.havelaar
 */
@Consumes(Constants.GWT_RPC_MEDIA_TYPE)
@Provider 
public class GwtRpcReader implements MessageBodyReader<Object> {
    
    private final static Logger logger = LoggerFactory.getLogger(GwtRpcReader.class);

    @Override
    public boolean isReadable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return mt.isCompatible(MediaTypes.GWT_RPC_MEDIA_TYPE);
    }

    @Override
    public Object readFrom(Class<Object> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, String> headers, InputStream in) throws IOException, WebApplicationException {
        try {
            String charset = mt.getParameters().get("charset");
            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8";
            }
            String data = GwtRpcUtil.streamToString(in, charset);
            if (data.isEmpty()) {
                throw new WebApplicationException(Response.Status.NO_CONTENT);
            }
            return GwtRpcUtil.deserialize(data);
        } catch (SerializationException ex) {
            logger.error("Failed to deserialize RPC data", ex);
            throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
        }
    }
}
