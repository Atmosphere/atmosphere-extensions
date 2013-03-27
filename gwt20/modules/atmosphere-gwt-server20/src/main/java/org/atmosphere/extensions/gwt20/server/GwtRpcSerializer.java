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
package org.atmosphere.extensions.gwt20.server;

import org.atmosphere.gwt.shared.server.GwtRpcUtil;
import com.google.gwt.user.client.rpc.SerializationException;
import java.io.IOException;
import java.io.OutputStream;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author p.havelaar
 */
public class GwtRpcSerializer implements Serializer {
    
    private final static Logger logger = LoggerFactory.getLogger(GwtRpcSerializer.class.getName());

    private final AtmosphereResource resource;
    private final String outputEncoding;

    public GwtRpcSerializer(AtmosphereResource resource) {
        this.resource = resource;
        this.outputEncoding = resource.getResponse().getCharacterEncoding();
    }

    @Override
    public void write(OutputStream out, Object o) throws IOException {
        String payload;
        try {
            //        if (!(o instanceof String)) {
//        } else {
//            payload = (String) o;
//        }
            payload = GwtRpcUtil.serialize(o);
        } catch (SerializationException ex) {
            throw new IOException("Failed to deserialize message");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Writing to outputstream with encoding: " + outputEncoding + " data: " + payload);
        }
        out.write(payload.getBytes(outputEncoding));
        out.flush();
    }

}
