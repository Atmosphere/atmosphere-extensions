package org.atmosphere.gwt20.jackson;

import java.io.IOException;
import java.util.Map;
import org.atmosphere.gwt20.server.SerializationException;
import org.atmosphere.gwt20.server.ServerSerializer;

/**
 *
 * @author p.havelaar
 */
public class JacksonSerializer implements ServerSerializer {

    final JacksonSerializerProvider provider;

    public JacksonSerializer(JacksonSerializerProvider provider) {
        this.provider = provider;
    }
    

    @Override
    public String serialize(Object data) throws SerializationException {
        try {
            return provider.mapper.writeValueAsString(data);
        } catch (IOException ex) {
            throw new SerializationException("Failed to serialize data", ex);
        }
    }
    
    @Override
    public Object deserialize(String data) throws SerializationException{
        try {
            // TODO not the most neat implementation
            return provider.mapper.readValue(data, Map.class);
        } catch (IOException ex) {
            throw new SerializationException("Failed to deserialize data", ex);
        }
    }
}
