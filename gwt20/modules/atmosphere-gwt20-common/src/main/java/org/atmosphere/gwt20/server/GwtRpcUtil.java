/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.gwt20.server;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author p.havelaar
 */
public class GwtRpcUtil implements ServerSerializerProvider {
   
   public final static String SERIALIZER_PROVIDER_NAME = "GwtRpc";

   @Override
   public String getName() {
      return SERIALIZER_PROVIDER_NAME;
   }
   
      
   @Override
   public ServerSerializer getServerSerializer() {
      return new ServerSerializer() {
         @Override
         public Object deserialize(String message) throws org.atmosphere.gwt20.server.SerializationException {
            try {
               return GwtRpcUtil.deserialize(message);
            } catch (SerializationException ex) {
               throw new org.atmosphere.gwt20.server.SerializationException(ex.getMessage(), ex);
            }
         }
         @Override
         public String serialize(Object message) throws org.atmosphere.gwt20.server.SerializationException {
            try {
               return GwtRpcUtil.serialize(message);
            } catch (SerializationException ex) {
               throw new org.atmosphere.gwt20.server.SerializationException(ex.getMessage(), ex);
            }
         }
      };
   }
    
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
    
    private final static SerializationPolicyProvider serializationPolicyProvider = new SerializationPolicyProvider() {
        @Override
        public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
            return serializationPolicy;
        }
    };
    
    public static SerializationPolicyProvider getSerializationPolicyProvider() {
        return serializationPolicyProvider;
    }
    
    public static SerializationPolicy getSerializationPolicy() {
        return serializationPolicy;
    }
    
    public static String streamToString(InputStream in, String charset) throws IOException, UnsupportedEncodingException {
        return readerToString(new BufferedReader(new InputStreamReader(in, charset)));
        // a possible other way
//        Scanner s = new Scanner(in, charset).useDelimiter("\\A");
//        String data = s.hasNext() ? s.next() : "";
    }
    
    public static String readerToString(BufferedReader r) throws IOException {
        
        StringBuilder data = new StringBuilder();

        char[] buf = new char[5120];
        int read = -1;
        while ((read = r.read(buf)) > 0) {
          data.append(buf, 0, read);
        }
        return data.toString();
    }

    public static Object deserialize(String data) throws SerializationException {
        ServerSerializationStreamReader reader = new ServerSerializationStreamReader(GwtRpcUtil.class.getClassLoader(), GwtRpcUtil.getSerializationPolicyProvider());
        reader.prepareToRead(data);
        return reader.readObject();
    }

    public static String serialize(Object message) throws SerializationException {
        ServerSerializationStreamWriter streamWriter = new ServerSerializationStreamWriter(GwtRpcUtil.getSerializationPolicy());
        streamWriter.prepareToWrite();
        streamWriter.writeObject(message);
        return streamWriter.toString();
    }

}
