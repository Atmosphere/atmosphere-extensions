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

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class AutoBeanClientSerializer implements ClientSerializer {

    private static final Logger logger = Logger.getLogger(AutoBeanClientSerializer.class.getName());

    private Map<Class, AutoBeanFactory> beanFactories;
    private AutoBeanFactory activeBeanFactory;
    private Class<Object> activeBeanClass;
    // buffer in order to capture split messages
    private StringBuffer buffer = new StringBuffer(16100);

    public void registerBeanFactory(AutoBeanFactory factory, Class forBean) {
        if (beanFactories == null) {
            beanFactories = new HashMap<Class, AutoBeanFactory>();
        }
        beanFactories.put(forBean, factory);
        if (activeBeanFactory == null) {
            setActiveBeanFactory(forBean);
        }
    }

    public void setActiveBeanFactory(Class forBean) {
        if (beanFactories == null) {
            throw new IllegalStateException("No bean factory available");
        }
        AutoBeanFactory factory = beanFactories.get(forBean);
        if (factory == null) {
            throw new IllegalStateException("No bean factory available");
        }
        activeBeanFactory = factory;
        activeBeanClass = forBean;
    }

    @Override
    public Object deserialize(String raw) throws SerializationException {

        buffer.append(raw); // TODO buffer messages in case we receive a chunked message

        // split up in different parts - based on the {}
        // this is necessary because multiple objects can be chunked in one single string
        int brackets = 0;
        int start = 0;
        List<String> messages = new ArrayList<String>();
        for (int i = 0; i < buffer.length(); i++) {

            // detect brackets
            if (buffer.charAt(i) == '{') {
                ++brackets;
            } else if (buffer.charAt(i) == '}') --brackets;

            // new message
            if (brackets == 0) {
                messages.add(buffer.substring(start, i + 1));
                start = i + 1;
            }
        }
        buffer.delete(0, start);

        // create the objects
        List<Object> objects = new ArrayList<Object>(messages.size());
        for (String message : messages) {
            try {

                logger.info("Deserialize " + message + " from " + raw);
                Object event = AutoBeanCodex.decode(activeBeanFactory, activeBeanClass, message).as();

                objects.add(event);

            } catch (RuntimeException e) {

                throw new SerializationException(e);

            }
        }
        return objects;
    }

    @Override
    public String serialize(Object message) throws SerializationException {
        try {

            AutoBean<Object> bean = AutoBeanUtils.getAutoBean(message);

            return AutoBeanCodex.encode(bean).getPayload();

        } catch (RuntimeException e) {

            throw new SerializationException(e);

        }
    }


}
