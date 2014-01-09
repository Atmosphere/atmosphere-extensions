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
package org.atmosphere.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AtmosphereObjectFactory} for Guice
 *
 * @author Jean-Francois Arcand
 */
public class GuiceObjectFactory implements AtmosphereObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(GuiceObjectFactory.class);

    private static Injector injector;

    @Override
    public <T, U extends T> U newClassInstance(AtmosphereFramework framework, Class<T> classType, Class<U> classToInstantiate) throws InstantiationException, IllegalAccessException {
        initInjector(framework);
        if (injector == null) {
            logger.warn("No Guice Injector found in current ServletContext. Are you using {}?", AtmosphereGuiceServlet.class.getName());
            logger.trace("Unable to find {}. Creating the object directly.", classToInstantiate.getName());
            return classToInstantiate.newInstance();
        } else {
            return injector.getInstance(classToInstantiate);
        }
    }

    public String toString() {
        return "Guice ObjectFactory";
    }

    private void initInjector(AtmosphereFramework framework) {
        if (injector == null) {
            com.google.inject.Injector servletInjector = (com.google.inject.Injector)
                    framework.getServletContext().getAttribute(com.google.inject.Injector.class.getName());

            if (servletInjector != null) {
                injector = servletInjector;
            } else {
                logger.trace("Creating the Guice injector manually with an empty AbstractModule");
                injector = Guice.createInjector(new AbstractModule() {
                    @Override
                    protected void configure() {
                    }
                });
            }
        }
    }
}
