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

package org.atmosphere.gwt20.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import java.util.HashMap;
import java.util.Map;

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
public class AutoBeanClientSerializer implements ClientSerializer {
    
    private Map<Class, AutoBeanFactory> beanFactories;
    private AutoBeanFactory activeBeanFactory;
    private Class<Object> activeBeanClass;
    
    public void registerBeanFactory(Class<AutoBeanFactory> factoryClass, Class forBean) {
        registerBeanFactory((AutoBeanFactory)GWT.create(factoryClass), forBean);
    }
    
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
    public Object deserialize(String message) throws SerializationException {
        try {

            Object event = AutoBeanCodex.decode(activeBeanFactory, activeBeanClass, message).as();

            return event;

        } catch (RuntimeException e) {

            throw new SerializationException(e);

        }
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
