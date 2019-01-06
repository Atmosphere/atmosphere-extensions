/*
* Copyright 2008-2019 Async-IO.org
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
/*
 * Copyright 2009 Richard Zschech.
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

package org.atmosphere.gwt20.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The base class for serializers. To instantiate this class follow this example:
 * <pre><code>
 * <p/>
 * {@literal @SerialTypes({ MyType1.class, MyType2.class })}
 * public abstract class MyCometSerializer extends AtmosphereGWTSerializer {}
 * <p/>
 * AtmosphereGWTSerializer serializer = GWT.create(MyCometSerializer.class);
 * AtmosphereClient client = new AtmosphereClient(url, serializer, listener);
 * </code></pre>
 * <p/>
 * Where MyType1 and MyType2 are the types that your expecting to receive from the server.
 * If you have a class hierarchy of messages that you want to send you only need to supply the base class here.
 * <p/>
 * For instance:
 * <pre><code>
 * public class Message {}
 * <p/>
 * public class MessageA extends Message {}
 * <p/>
 * public class MessageB extends Message {}
 * <p/>
 * {@literal @SerialTypes( Message.class )}
 * public abstract class MyCometSerializer extends AtmosphereGWTSerializer {}
 * <p/>
 * </code></pre>
 */
public abstract class GwtRpcClientSerializer implements ClientSerializer {

    private static final Logger logger = Logger.getLogger(GwtRpcClientSerializer.class.getName());

    // buffer in order to capture split messages
    private StringBuffer buffer = new StringBuffer(16100);

    @Override
    public Object deserialize(String raw) throws SerializationException {

        List<String> messages = new ArrayList<String>();
        messages.add(raw);

        // create the objects
        List<Object> objects = new ArrayList<Object>(messages.size());
        for (String message : messages) {
            try {
                Serializer serializer = getRPCSerializer();
                ClientSerializationStreamReader reader = new ClientSerializationStreamReader(serializer);
                reader.prepareToRead(message);
                objects.add(reader.readObject());
            } catch (RuntimeException e) {
                throw new SerializationException(e);
            }
        }

        return objects;
    }

    @Override
    public String serialize(Object message) throws SerializationException {
        try {
            Serializer serializer = getRPCSerializer();
            ClientSerializationStreamWriter writer = new ClientSerializationStreamWriter(serializer, GWT.getModuleBaseURL(), GWT.getPermutationStrongName());
            writer.prepareToWrite();
            writer.writeObject(message);
            return writer.toString();
        } catch (RuntimeException e) {
            throw new SerializationException(e);
        }
    }

    protected abstract Serializer getRPCSerializer();
}