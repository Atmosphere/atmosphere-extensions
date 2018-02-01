package org.atmosphere.spring;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.InjectableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.beans.Introspector;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AtmosphereObjectFactory} for Spring to be used in Servlet Base Java Apps.
 *
 * @author Aparup Banerjee
 */
public class SpringWebObjectFactory extends InjectableObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringWebObjectFactory.class);
    protected boolean preventSpringInjection = false;
    private final List<Class<?>> excludedFromInjection = new ArrayList<Class<?>>();

    private AnnotationConfigApplicationContext context;

    @Override
    public <T, U extends T> U newClassInstance(Class<T> classType,
                                               Class<U> classToInstantiate)
            throws InstantiationException, IllegalAccessException {

        if (preventSpringInjection && excludedFromInjection.contains(classType)) {
            logger.trace("Excluded from injection {}", classToInstantiate.getName());
            return classToInstantiate.newInstance();
        }

        String name = classToInstantiate.getSimpleName();
        if (!context.containsBeanDefinition(Introspector.decapitalize(name))) {
            context.register(classToInstantiate);
        }

        U t;
        try {
            t = context.getAutowireCapableBeanFactory().createBean(classToInstantiate);
        } catch (BeanCreationException e) {
            // Fallback to Atmosphere instead of writing all kind of Spring glue code.
            logger.warn("Unable to create bean {}", classToInstantiate.getName(), e);
            t = super.newClassInstance(classType, classToInstantiate);
        }

        if (t == null) {
            logger.info("Unable to find {}. Creating the object directly."
                    + classToInstantiate.getName());
            return classToInstantiate.newInstance();
        }
        return t;
    }

    @Override
    public AtmosphereObjectFactory allowInjectionOf(Injectable<?> injectable) {
        context.register((Class<?>) ((ParameterizedType) injectable.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        return this;
    }

    public String toString() {
        return "Spring Web ObjectFactory";
    }

    @Override
    public void configure(AtmosphereConfig config) {
        super.configure(config);
        try {
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
