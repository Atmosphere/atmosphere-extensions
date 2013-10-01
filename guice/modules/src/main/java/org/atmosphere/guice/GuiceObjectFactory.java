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

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;

/**
 * An {@link AtmosphereObjectFactory} for Guice
 *
 * @author Jean-Francois Arcand
 */
public class GuiceObjectFactory implements AtmosphereObjectFactory {
    @Override
    public <T> T newClassInstance(AtmosphereFramework framework, Class<T> tClass) throws InstantiationException, IllegalAccessException {
        com.google.inject.Injector injector = (com.google.inject.Injector)
                framework.getServletContext().getAttribute(com.google.inject.Injector.class.getName());
        if (injector == null)
            throw new IllegalStateException("No Guice Injector found in current ServletContext !");
        return injector.getInstance(tClass);
    }
}
