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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author p.havelaar
 */
public class GwtRpcUtil implements ServerSerializerProvider {

    public final static String SERIALIZER_PROVIDER_NAME = "GwtRpc";

    /**
     * A reference to the annotation class
     * javax.jdo.annotations.PersistenceCapable used by the JDO API. May be null
     * if JDO is not present in the runtime environment.
     */
    private static Class<? extends Annotation> JDO_PERSISTENCE_CAPABLE_ANNOTATION = null;

    /**
     * A reference to the method 'String
     * javax.jdo.annotations.PersistenceCapable.detachable()'.
     */
    private static Method JDO_PERSISTENCE_CAPABLE_DETACHABLE_METHOD;

    /**
     * A reference to the annotation class javax.persistence.Entity used by the
     * JPA API. May be null if JPA is not present in the runtime environment.
     */
    private static Class<? extends Annotation> JPA_ENTITY_ANNOTATION = null;

	static {
		try {
			JDO_PERSISTENCE_CAPABLE_ANNOTATION = Class.forName("javax.jdo.annotations.PersistenceCapable").asSubclass(Annotation.class);
			JDO_PERSISTENCE_CAPABLE_DETACHABLE_METHOD = JDO_PERSISTENCE_CAPABLE_ANNOTATION.getDeclaredMethod("detachable", (Class[]) null);
		} catch (ClassNotFoundException e) {
			// Ignore, JDO_PERSISTENCE_CAPABLE_ANNOTATION will be null
		} catch (NoSuchMethodException e) {
			JDO_PERSISTENCE_CAPABLE_ANNOTATION = null;
		}

		try {
			JPA_ENTITY_ANNOTATION = Class.forName("javax.persistence.Entity").asSubclass(Annotation.class);
		} catch (ClassNotFoundException e) {
			// Ignore, JPA_ENTITY_CAPABLE_ANNOTATION will be null
		}
	}

	static boolean hasJpaAnnotation(Class<?> clazz) {
		if (JPA_ENTITY_ANNOTATION == null) {
			return false;
		}
		return clazz.getAnnotation(JPA_ENTITY_ANNOTATION) != null;
	}
	
	static boolean hasJdoAnnotation(Class<?> clazz) {
		if (JDO_PERSISTENCE_CAPABLE_ANNOTATION == null) {
			return false;
		}
		Annotation annotation = clazz.getAnnotation(JDO_PERSISTENCE_CAPABLE_ANNOTATION);
		if (annotation == null) {
			return false;
		}
		try {
			Object value = JDO_PERSISTENCE_CAPABLE_DETACHABLE_METHOD.invoke(annotation, (Object[]) null);
			if (value instanceof String) {
				return "true".equalsIgnoreCase((String) value);
			} else {
				return false;
			}
		} catch (IllegalAccessException e) {
			// will return false
		} catch (InvocationTargetException e) {
			// will return false
		}
		return false;
	}

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
    	public Set<String> getClientFieldNamesForEnhancedClass(Class<?> clazz) {
    		if (hasJpaAnnotation(clazz) || hasJdoAnnotation(clazz)) {
    			Set<String> fieldNames = new TreeSet<String>();
    			for (Field f : clazz.getDeclaredFields()) {
    				fieldNames.add(f.getName());
    			}
    			return fieldNames;
    		} else {
    			return null;
    		}
    	}
    	
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
