/*
 * Copyright 2008-2020 Async-IO.org
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
package org.atmosphere.cdi;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.inject.AtmosphereProducers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Iterator;

/**
 * CDI support for injecting object
 *
 * @author Jeanfrancois Arcand
 */
public class CDIObjectFactory implements AtmosphereObjectFactory<Object> {

    private static final Logger logger = LoggerFactory.getLogger(CDIObjectFactory.class);

    private BeanManager bm;

    public CDIObjectFactory(){
        try {
            bm = (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        } catch (NamingException ex) {
            try {
                bm = (BeanManager) new InitialContext().lookup("java:comp/env/BeanManager");
            } catch (NamingException e) {
                logger.error("{}", e);
                throw new IllegalStateException();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, U extends T> U newClassInstance(Class<T> classType, Class<U> classToInstantiate) throws InstantiationException, IllegalAccessException {
        CreationalContext cc = null;

        try {
            final Iterator<Bean<?>> i = bm.getBeans(classToInstantiate).iterator();
            if (!i.hasNext()) {
                logger.trace("Unable to find {}. Creating the object directly.", classToInstantiate.getName());
                return classToInstantiate.newInstance();
            }
            Bean<U> bean = (Bean<U>) i.next();
            CreationalContext<U> ctx = bm.createCreationalContext(bean);
            U dao = (U) bm.getReference(bean, classToInstantiate, ctx);

            return dao;
        } catch (Exception e) {
            logger.error("Unable to construct {}. Creating the object directly.", classToInstantiate.getName());
            return classToInstantiate.newInstance();
        } finally {
            if (cc != null) cc.release();
        }
    }

    @Override
    public AtmosphereObjectFactory allowInjectionOf(Object o) {
        return this;
    }


    public String toString() {
        return "CDI ObjectFactory";
    }

    @Override
    public void configure(AtmosphereConfig config) {
        try {
            AtmosphereProducers p = newClassInstance(AtmosphereProducers.class,AtmosphereProducers.class);
            p.configure(config);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
