/*
 * Copyright 2014 Jeanfrancois Arcand
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
import com.google.inject.Provider;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.MetaBroadcaster;
import org.atmosphere.inject.AtmosphereProducers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AtmosphereObjectFactory} for Guice
 *
 * @author Jean-Francois Arcand
 */
public class GuiceObjectFactory implements AtmosphereObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(GuiceObjectFactory.class);

    protected Injector injector;
    protected AtmosphereConfig config;

    @Override
    public <T, U extends T> U newClassInstance(Class<T> classType, Class<U> classToInstantiate) throws InstantiationException, IllegalAccessException {
        initInjector(config.framework());
        U t;
        if (injector == null) {
            logger.warn("No Guice Injector found in current ServletContext. Are you using {}?", AtmosphereGuiceServlet.class.getName());
            logger.trace("Unable to find {}. Creating the object directly.", classToInstantiate.getName());
            t = classToInstantiate.newInstance();
        } else {
            t = injector.getInstance(classToInstantiate);
        }

        return t;
    }

    public String toString() {
        return "Guice ObjectFactory";
    }

    protected void initInjector(AtmosphereFramework framework) {
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

            injector = injector.createChildInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BroadcasterFactory.class).toProvider(new Provider<BroadcasterFactory>() {
                        @Override
                        public BroadcasterFactory get() {
                            return config.getBroadcasterFactory();
                        }
                    });
                    bind(AtmosphereFramework.class).toProvider(new Provider<AtmosphereFramework>() {
                        @Override
                        public AtmosphereFramework get() {
                            return config.framework();
                        }
                    });
                    bind(AtmosphereResourceFactory.class).toProvider(new Provider<AtmosphereResourceFactory>() {
                        @Override
                        public AtmosphereResourceFactory get() {
                            return config.resourcesFactory();
                        }
                    });
                    bind(MetaBroadcaster.class).toProvider(new Provider<MetaBroadcaster>() {
                        @Override
                        public MetaBroadcaster get() {
                            return config.metaBroadcaster();
                        }
                    });
                    bind(AtmosphereResourceSessionFactory.class).toProvider(new Provider<AtmosphereResourceSessionFactory>() {
                        @Override
                        public AtmosphereResourceSessionFactory get() {
                            return config.sessionFactory();
                        }
                    });
                    bind(AtmosphereConfig.class).toProvider(new Provider<AtmosphereConfig>() {
                        @Override
                        public AtmosphereConfig get() {
                            return config;
                        }
                    });

                }
            });
        }
    }

    @Override
    public void configure(AtmosphereConfig config) {
        this.config = config;

        try {
            AtmosphereProducers p = newClassInstance(AtmosphereProducers.class,AtmosphereProducers.class);
            p.configure(config);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
