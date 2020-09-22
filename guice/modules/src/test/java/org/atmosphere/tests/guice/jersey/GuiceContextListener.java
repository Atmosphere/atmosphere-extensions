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
package org.atmosphere.tests.guice.jersey;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.atmosphere.container.Jetty7CometSupport;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.guice.AtmosphereGuiceServlet;
import org.atmosphere.guice.GuiceObjectFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GuiceContextListener extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                bind(PubSubTest.class);
                bind(new TypeLiteral<Map<String, String>>() {
                }).annotatedWith(Names.named(AtmosphereGuiceServlet.PROPERTIES)).toInstance(
                                        Collections.<String, String>emptyMap());
                serve("/*").with(AtmosphereGuiceServlet.class, new HashMap<String, String>() {
                    {
                        put(ApplicationConfig.PROPERTY_COMET_SUPPORT, Jetty7CometSupport.class.getName());
                        put(ApplicationConfig.OBJECT_FACTORY, GuiceObjectFactory.class.getName());
                        put(ApplicationConfig.ANALYTICS, "false");
                        put("org.atmosphere.useNative", "true");
                        put("com.sun.jersey.config.property.packages", PubSubTest.class.getPackage().getName());
                    }
                });
            }
        });
    }
}
