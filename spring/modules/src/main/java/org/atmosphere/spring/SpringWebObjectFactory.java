package org.atmosphere.spring;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * An {@link AtmosphereObjectFactory} for Spring to be used in Servlet Base Java Apps.
 *
 * @author Aparup Banerjee
 */
public class SpringWebObjectFactory implements AtmosphereObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringWebObjectFactory.class);
    private AtmosphereConfig config;
    private AnnotationConfigApplicationContext context;

    @Override
    public <T, U extends T> U newClassInstance(Class<T> classType,
                                               Class<U> classToInstantiate)
            throws InstantiationException, IllegalAccessException {

        String name = classToInstantiate.getSimpleName();
        if (!context.containsBeanDefinition(name.replace(name.substring(0,1), name.substring(0,1).toLowerCase()))) {
            context.register(classToInstantiate);
        }
        U t = context.getBean(classToInstantiate);

        if (t == null) {
            logger.info("Unable to find {}. Creating the object directly."
                    + classToInstantiate.getName());
            return classToInstantiate.newInstance();
        }
        return t;
    }

    public String toString() {
        return "Spring Web ObjectFactory";
    }

    @Override
    public void configure(AtmosphereConfig config) {
        this.config = config;

        context = new AnnotationConfigApplicationContext();
        context.setParent(WebApplicationContextUtils.getWebApplicationContext(config.framework().getServletContext()));

        context.refresh();

        // Hack to make it injectable
        context.register(AtmosphereConfig.class);
        context.getBean(AtmosphereConfig.class, config.framework()).populate(config);
    }
}
