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

package org.atmosphere.extensions.gwtwrapper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;

/**
 * The base class for serializers. To instantiate this class follow this example:
 * <pre><code>
 *
 * {@literal @SerialTypes({ MyType1.class, MyType2.class })}
 * public abstract class MyCometSerializer extends AtmosphereGWTSerializer {}
 * 
 * AtmosphereGWTSerializer serializer = GWT.create(MyCometSerializer.class);
 * AtmosphereClient client = new AtmosphereClient(url, serializer, listener);
 * </code></pre>
 *
 * Where MyType1 and MyType2 are the types that your expecting to receive from the server.
 * If you have a class hierarchy of messages that you want to send you only need to supply the base class here.
 * 
 * For instance:
 * <pre><code>
 * public class Message {}
 * 
 * public class MessageA extends Message {}
 * 
 * public class MessageB extends Message {}
 * 
 * {@literal @SerialTypes( Message.class )}
 * public abstract class MyCometSerializer extends AtmosphereGWTSerializer {}
 * 
 * </code></pre>
 */
public abstract class GwtClientSerializer {
    
    public Object deserialize(String message) throws SerializationException {
        try {
            Serializer serializer = getRPCSerializer();
            ClientSerializationStreamReader reader = new ClientSerializationStreamReader(serializer);
            reader.prepareToRead(message);
            return reader.readObject();
        } catch (RuntimeException e) {
            throw new SerializationException(e);
        }
    }

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
