/*
 * Copyright 2015 Async-IO.org
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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.ReflectorServletProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Map;

/**
 * Google Guice Integration. Only difference with the {@link AtmosphereServlet} is this class is annotated with @Singleton
 * annotation.
 *
 * @author Jeanfrancois Arcand
 */
@Singleton
public class AtmosphereGuiceServlet extends AtmosphereServlet {
    public static final String PROPERTIES = GuiceObjectFactory.class.getName() + ".properties";

    protected AtmosphereServlet configureFramework(ServletConfig sc, boolean init) throws ServletException {
        initializer.configureFramework(sc, init, false, GuiceAtmosphereFramework.class);
        return this;
    }

    public final class GuiceAtmosphereFramework extends AtmosphereFramework {
        @Override
        protected void configureDetectedFramework(ReflectorServletProcessor rsp, boolean isJersey) {
            if (isJersey) {
                Injector injector = (Injector) framework().getAtmosphereConfig().getServletContext().getAttribute(Injector.class.getName());
                GuiceContainer guiceServlet = injector.getInstance(GuiceContainer.class);
                rsp.setServlet(guiceServlet);

                try {
                    Map<String, String> props = injector.getInstance(
                            Key.get(new TypeLiteral<Map<String, String>>() {
                            }, Names.named(PROPERTIES)));

                    if (props != null) {
                        for (String p : props.keySet()) {
                            framework().addInitParameter(p, props.get(p));
                        }
                    }
                } catch (Exception ex) {
                    // Do not fail
                    logger.debug("failed to add Jersey init parameters to Atmosphere servlet", ex);
                }
            }
        }
    }
}
