package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class MailService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    /**
     * The email address to use for sending emails as defined in the application properties file.
     */
    @Value("${mail.from}")
    private String email;

    /**
     * The mail sender to use for sending emails.
     */
    private final JavaMailSender emailSender;

    /**
     * The template engine to use for processing mail templates.
     */
    private final SpringTemplateEngine templateEngine;

    /**
     * Constructs a new mail service with the given dependencies.
     *
     * @param emailSender The mail sender to use.
     * @param templateEngine The template engine to use.
     */
    @Autowired
    public MailService(@Qualifier("mailSender") final JavaMailSender emailSender,
                       final SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends the given email template to the given addresses. If the {@link MailService} fails to send the message
     * three consecutive times, it stops.
     *
     * @param to The recipient of the email.
     * @param subject The subject of this email.
     * @param templateModel The template model containing additional properties.
     * @param template The name of the mail template to use.
     * @return {@code true} if the email was sent successfully, or {@code false} otherwise.
     */
    public boolean sendEmail(final String to, final String subject, final Map<String, Object> templateModel,
                              final String template) {
        int tries = 0;

        while (tries < Constants.MAX_EMAIL_TRIES) {
            try {
                sendTemplateMessage(to, null, null, null, subject, templateModel, template);
                return true;
            } catch (MessagingException e) {
                tries++;
                logger.error("Failed to send message to address " + to + " on try #" + tries + "!", e);
            }
        }

        return false;
    }

    /**
     * Sends a new template message to the given addresses with the content specified in the given template. If the
     * email could not be sent, a {@link MessagingException} is thrown instead.
     *
     * @param to The recipient of the email.
     * @param cc The address to which a copy of this email should be send.
     * @param bcc The address to which a blind copy of this email should be send.
     * @param replyTo The reply to address.
     * @param subject The subject of this email.
     * @param templateModel The template model containing additional properties.
     * @param template The name of the mail template to use.
     * @throws MessagingException If the email could not be sent.
     */
    public void sendTemplateMessage(final String to, final String cc, final String bcc, final String replyTo,
                                    final String subject, final Map<String, Object> templateModel,
                                    final String template) throws MessagingException {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        String htmlBody = templateEngine.process(template, thymeleafContext);

        sendHtmlMessage(to, cc, bcc, replyTo, subject, htmlBody);
    }

    /**
     * Sends the given html message to the given addresses. If the email could not be sent, a {@link MessagingException}
     * is thrown instead.
     *
     * @param to The recipient of the email.
     * @param cc The address to which a copy of this email should be send.
     * @param bcc The address to which a blind copy of this email should be send.
     * @param replyTo The reply to address.
     * @param subject The subject of this email.
     * @param htmlBody The html representation of the mail template.
     * @throws MessagingException If the email could not be sent.
     */
    private void sendHtmlMessage(final String to, final String cc, final String bcc, final String replyTo,
                                 final String subject, final String htmlBody) throws MessagingException {
        logger.debug("Sending email to " + to + " with subject " + subject);
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(email);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        if (cc != null) {
            helper.setCc(cc);
        }
        if (bcc != null) {
            helper.setBcc(bcc);
        }
        if (replyTo != null) {
            helper.setReplyTo(replyTo);
        }

        emailSender.send(message);
    }

}
