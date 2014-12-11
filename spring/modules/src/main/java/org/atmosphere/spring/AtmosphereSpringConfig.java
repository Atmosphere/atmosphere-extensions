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
package org.atmosphere.spring;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.MetaBroadcaster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = {"org.atmosphere.spring"})
public class AtmosphereSpringConfig {

    private final AtmosphereConfig config;

    public AtmosphereSpringConfig(AtmosphereConfig config) {
        this.config = config;
    }

    @Bean
    public BroadcasterFactory getBroadcasterFactory() {
        return config.getBroadcasterFactory();
    }

    @Bean
    public AtmosphereResourceFactory getAtmosphereResourceFactory() {
        return config.resourcesFactory();
    }

    @Bean
    public AtmosphereResourceSessionFactory getAtmosphereResourceSessionFactory() {
        return config.sessionFactory();
    }

    @Bean
    public AtmosphereConfig getAtmosphereConfig() {
        return config;
    }

    @Bean
    public AtmosphereFramework getAtmosphereFramework() {
        return config.framework();
    }

    @Bean
    public MetaBroadcaster getMetaBroadcaster() {
        return config.metaBroadcaster();
    }

}
