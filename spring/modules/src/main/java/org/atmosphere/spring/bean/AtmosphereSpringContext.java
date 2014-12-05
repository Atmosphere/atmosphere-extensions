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
package org.atmosphere.spring.bean;

import org.atmosphere.util.ServletProxyFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Spring context.
 *
 * @author Evgeny Konovalov
 */
public class AtmosphereSpringContext implements ServletConfig {

    private Map<String, String> config;

    @Override
    public String getServletName() {
        return "AtmosphereFramework";
    }

    @Override
    public ServletContext getServletContext() {
        return (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ServletContext.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return ServletProxyFactory.getDefault().proxy(proxy, method, args);
                    }
                }
        );
    }

    @Override
    public String getInitParameter(String s) {
        return config.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(config.values());
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

}

