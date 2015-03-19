package org.atmosphere.spring;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.cpr.AtmosphereResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.beans.Introspector;

/**
 * An {@link AtmosphereObjectFactory} for Spring to be used in Servlet Base Java Apps.
 *
 * @author Aparup Banerjee
 */
public class SpringWebObjectFactory implements AtmosphereObjectFactory<Class<?>> {

    private static final Logger logger = LoggerFactory.getLogger(SpringWebObjectFactory.class);

    protected boolean allowAtmosphereResourceInjection = false;
    public static String SPRING_INJECT_ATMOSPHERERESOURCE = "org.atmosphere.spring.ar.inject";

    private AnnotationConfigApplicationContext context;

    @Override
    public <T, U extends T> U newClassInstance(Class<T> classType,
                                               Class<U> classToInstantiate)
            throws InstantiationException, IllegalAccessException {

        if (!allowAtmosphereResourceInjection && classType.getName().equals(AtmosphereResource.class.getName()))
            return classToInstantiate.newInstance();

        String name = classToInstantiate.getSimpleName();
        if (!context.containsBeanDefinition(Introspector.decapitalize(name))) {
            context.register(classToInstantiate);
        }
        U t = context.getAutowireCapableBeanFactory().createBean(classToInstantiate);

        if (t == null) {
            logger.info("Unable to find {}. Creating the object directly."
                    + classToInstantiate.getName());
            return classToInstantiate.newInstance();
        }
        return t;
    }

    @Override
    public AtmosphereObjectFactory allowInjectionOf(Class<?> aClass) {
        context.register(aClass);
        return this;
    }

    public String toString() {
        return "Spring Web ObjectFactory";
    }

    @Override
    public void configure(AtmosphereConfig config) {
        try {

            String s = config.getInitParameter(SPRING_INJECT_ATMOSPHERERESOURCE);
            if (s != null) {
                allowAtmosphereResourceInjection = Boolean.parseBoolean(s);
            }

            context = new AnnotationConfigApplicationContext();
            context.setParent(WebApplicationContextUtils.getWebApplicationContext(config.framework().getServletContext()));

            context.refresh();

            // Hack to make it injectable
            context.register(AtmosphereConfig.class);
            context.getBean(AtmosphereConfig.class, config.framework()).populate(config);
        } catch (Exception ex) {
            logger.warn("Unable to configure injection", ex);
        }
    }
}
