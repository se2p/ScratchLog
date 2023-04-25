package fim.unipassau.de.scratch1984.spring.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The resource configuration for Spring.
 */
@Configuration
public class ResourceConfiguration implements WebMvcConfigurer {

    /**
     * Registers all the resource handlers needed to serve static resources at runtime.
     *
     * @param registry The registry storing the necessary information.
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");
    }

    /**
     * Registers all the view controllers to forward to templates at runtime.
     *
     * @param registry The registry storing the necessary information.
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addRedirectViewController("/login/saml2", "/saml2/login");
    }

}
