package org.atmosphere.extensions.gwtwrapper.server;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Serializer;

/**
 *
 * @author p.havelaar
 */
public class GwtRpcSerializer implements Serializer {
    
    private final static Logger logger = Logger.getLogger(GwtRpcSerializer.class.getName());

    private final AtmosphereResource resource;
    private final String outputEncoding;

    private final static SerializationPolicy serializationPolicy = new SerializationPolicy() {
        @Override
        public boolean shouldDeserializeFields(final Class<?> clazz) {
            return Object.class != clazz;
        }

        @Override
        public boolean shouldSerializeFields(final Class<?> clazz) {
            return Object.class != clazz;
        }

        @Override
        public void validateDeserialize(final Class<?> clazz) {
        }

        @Override
        public void validateSerialize(final Class<?> clazz) {
        }
    };
    
    private final SerializationPolicyProvider serializationPolicyProvider = new SerializationPolicyProvider() {
        @Override
        public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
            return serializationPolicy;
        }
    };

    public GwtRpcSerializer(AtmosphereResource resource) {
        this.resource = resource;
        this.outputEncoding = resource.getResponse().getCharacterEncoding();
    }

    @Override
    public void write(OutputStream out, Object o) throws IOException {
        String payload;
        if (!(o instanceof String)) {
            payload = serialize(o);
        } else {
            payload = (String) o;
        }
        out.write(payload.getBytes(outputEncoding));
    }
    
    public String serialize(Object message) {
        try {
            ServerSerializationStreamWriter streamWriter = new ServerSerializationStreamWriter(serializationPolicy);
            streamWriter.prepareToWrite();
            streamWriter.writeObject(message);
            return streamWriter.toString();
        } catch (SerializationException ex) {
            logger.log(Level.SEVERE, "Failed to serialize message", ex);
            return null;
        }
    }

    public Object deserialize(String data) {
        try {
            ServerSerializationStreamReader reader = new ServerSerializationStreamReader(getClass().getClassLoader(), serializationPolicyProvider);
            reader.prepareToRead(data);
            return reader.readObject();
        } catch (SerializationException ex) {
            logger.log(Level.SEVERE, "Failed to deserialize RPC data", ex);
            return null;
        }
    }

}
