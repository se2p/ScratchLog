/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.spring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import java.util.Properties;

/**
 * The mail configuration for sending emails.
 */
@Configuration
@PropertySource(value = {"classpath:application.properties"})
public class MailConfiguration {

    /**
     * The mail server host as defined in the application properties file.
     */
    @Value("${spring.mail.host}")
    private String mailServerHost;

    /**
     * The mail server port as defined in the application properties file.
     */
    @Value("${spring.mail.port}")
    private Integer mailServerPort;

    /**
     * The mail server username as defined in the application properties file.
     */
    @Value("${spring.mail.username}")
    private String mailServerUsername;

    /**
     * The mail server password as defined in the application properties file.
     */
    @Value("${spring.mail.password}")
    private String mailServerPassword;

    /**
     * Whether or not to use mail server authentication as defined in the application properties file.
     */
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String mailServerAuth;

    /**
     * Whether or not to use tls as defined in the application properties file.
     */
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String mailServerStartTls;

    /**
     * The path to the mail templates as defined in the application properties file.
     */
    @Value("${spring.mail.templates.path}")
    private String mailTemplatesPath;

    /**
     * Configures the {@link JavaMailSender} to use for sending emails with the properties defined in the application
     * properties file.
     *
     * @return The configured mail sender.
     */
    @Bean(name = "mailSender")
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(mailServerHost);
        mailSender.setPort(mailServerPort);

        mailSender.setUsername(mailServerUsername);
        mailSender.setPassword(mailServerPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailServerAuth);
        props.put("mail.smtp.starttls.enable", mailServerStartTls);
        props.put("mail.debug", "true");

        return mailSender;
    }

    /**
     * Configures the {@link ITemplateResolver} to locate the mail templates.
     *
     * @return The configured template resolver.
     */
    @Bean
    public ITemplateResolver thymeleafClassLoaderTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix(mailTemplatesPath + "/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCheckExistence(true);
        return templateResolver;
    }

    /**
     * Injects the previously configured template resolver to use with thymeleaf.
     *
     * @return The configured message source.
     */
    @Bean
    public ResourceBundleMessageSource emailMessageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("mailMessages");
        return messageSource;
    }

}
