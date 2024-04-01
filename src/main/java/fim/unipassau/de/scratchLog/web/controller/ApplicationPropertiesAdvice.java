package fim.unipassau.de.scratchLog.web.controller;

import fim.unipassau.de.scratchLog.util.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects some global model attributes into each controller.
 */
@ControllerAdvice(annotations = Controller.class)
public class ApplicationPropertiesAdvice {

    /**
     * The application properties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Autowiring constructor.
     *
     * @param applicationProperties The {@link ApplicationProperties}.
     */
    @Autowired
    public ApplicationPropertiesAdvice(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Injects the application properties as {@code applicationConfig} into the model for all controllers.
     *
     * @return The global application config.
     */
    @ModelAttribute("applicationConfig")
    public final ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

}
