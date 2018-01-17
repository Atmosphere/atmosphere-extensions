/*
 * Copyright 2018 Async-IO.org
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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.handler.ReflectorServletProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Map;

import static org.atmosphere.guice.AtmosphereGuiceServlet.PROPERTIES;

/**
 * Google Guice Integration.
 *
 * @author Jeanfrancois Arcand
 */
public class GuiceAtmosphereFramework extends AtmosphereFramework {

    public GuiceAtmosphereFramework() {
        this(false, true);
    }

    public GuiceAtmosphereFramework(ServletConfig sc) throws ServletException {
        super(false, true);
    }

    public GuiceAtmosphereFramework(boolean isFilter, boolean autoDetectHandlers) {
        super(isFilter, autoDetectHandlers);
    }

    @Override
    protected void configureDetectedFramework(ReflectorServletProcessor rsp, boolean isJersey) {
        if (isJersey) {
            logger.info("Configuring Guice for Atmosphere Jersey");
            Injector injector = (Injector) getAtmosphereConfig().getServletContext().getAttribute(Injector.class.getName());
            GuiceContainer guiceServlet = injector.getInstance(GuiceContainer.class);
            rsp.setServlet(guiceServlet);

            try {
                Map<String, String> props = injector.getInstance(
                        Key.get(new TypeLiteral<Map<String, String>>() {
                        }, Names.named(PROPERTIES)));

                if (props != null) {
                    for (String p : props.keySet()) {
                        addInitParameter(p, props.get(p));
                    }
                }
            } catch (Exception ex) {
                // Do not fail
                logger.debug("failed to add Jersey init parameters to Atmosphere servlet", ex.getCause());
            }
        }
    }
}
