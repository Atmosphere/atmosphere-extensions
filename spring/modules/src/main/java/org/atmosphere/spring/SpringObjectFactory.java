/*
 * Copyright 2014 Jason Burgess
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
package org.atmosphere.spring;

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * An {@link AtmosphereObjectFactory} for Spring.
 *
 * @author Jean-Francois Arcand
 */
public class SpringObjectFactory implements AtmosphereObjectFactory {
    private static final Logger logger = LoggerFactory.getLogger(SpringObjectFactory.class);

    @Override
    public <T, U extends T> U newClassInstance(AtmosphereFramework framework, Class<T> classType, Class<U> classToInstantiate) throws InstantiationException, IllegalAccessException {
        ApplicationContext context =
            new AnnotationConfigApplicationContext(classToInstantiate);
        U t = context.getBean(classToInstantiate);
        if (t == null) {
            logger.info("Unable to find {}. Creating the object directly.", classToInstantiate.getName());
            return classToInstantiate.newInstance();
        }
        return t;
    }

    public String toString() {
        return "Spring ObjectFactory";
    }
}
