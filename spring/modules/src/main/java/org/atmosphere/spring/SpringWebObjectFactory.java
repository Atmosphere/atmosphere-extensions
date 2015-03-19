package org.atmosphere.spring;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AtmosphereObjectFactory} for Spring to be used in Servlet Base Java Apps.
 *
 * @author Aparup Banerjee
 */
public class SpringWebObjectFactory implements AtmosphereObjectFactory<Class<?>> {
    /**
     * A comma delimited list of {@link org.atmosphere.inject.InjectableObjectFactory.DEFAULT_ATMOSPHERE_INJECTABLE} that
     * won't be created by Spring.
     */
    public static final String SPRING_INJECT_ATMOSPHERERESOURCE = "org.atmosphere.spring.excludedClasses";

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
                String[] list = s.split(",");
                for (String clazz : list) {
                    excludedFromInjection.add(IOUtils.loadClass(getClass(), clazz));
                }

                if (list.length > 0) {
                    preventSpringInjection = true;
                }
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
