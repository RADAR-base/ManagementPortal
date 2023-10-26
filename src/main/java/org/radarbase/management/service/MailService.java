package org.radarbase.management.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import javax.mail.internet.MimeMessage;

import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

/**
 * Service for sending emails. <p> We use the @Async annotation to send emails asynchronously. </p>
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private static final String EXPIRY = "expiry";

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Send an email.
     * @param to email address to send to
     * @param subject subject line
     * @param content email contents
     * @param isMultipart send as multipart
     * @param isHtml send as html
     */
    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart,
            boolean isHtml) {
        log.debug(
                "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart,
                    StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(managementPortalProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (Exception e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }

    /**
     * Send an account activation email to a given user.
     * @param user the user to send to
     */
    @Async
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.email);
        Locale locale = Locale.forLanguageTag(user.langKey);
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL,
                managementPortalProperties.getCommon().getManagementPortalBaseUrl());
        String content = templateEngine.process("activationEmail", context);
        String subject = messageSource.getMessage("email.activation.title", null, locale);
        sendEmail(user.email, subject, content, false, true);
    }

    /**
     * Send account creation email to a given user.
     * @param user the user
     */
    @Async
    public void sendCreationEmail(User user, long duration) {
        log.debug("Sending creation email to '{}'", user.email);
        Locale locale = Locale.forLanguageTag(user.langKey);
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL,
                managementPortalProperties.getCommon().getManagementPortalBaseUrl());
        context.setVariable(EXPIRY, Duration.ofSeconds(duration).toHours());
        String content = templateEngine.process("creationEmail", context);
        String subject = messageSource.getMessage("email.activation.title", null, locale);
        sendEmail(user.email, subject, content, false, true);
    }

    /**
     * Send account creation email for a user to an address different than the users' address.
     * @param user the created user
     * @param email the address to send to
     */
    @Async
    public void sendCreationEmailForGivenEmail(User user, String email) {
        log.debug("Sending creation email to '{}'", email);
        Locale locale = Locale.forLanguageTag(user.langKey);
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL,
                managementPortalProperties.getCommon().getManagementPortalBaseUrl());
        String content = templateEngine.process("creationEmail", context);
        String subject = messageSource.getMessage("email.activation.title", null, locale);
        sendEmail(email, subject, content, false, true);
    }

    /**
     * Send a password reset email to a given user.
     * @param user the user
     */
    @Async
    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset email to '{}'", user.email);
        Locale locale = Locale.forLanguageTag(user.langKey);
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL,
                managementPortalProperties.getCommon().getManagementPortalBaseUrl());
        String content = templateEngine.process("passwordResetEmail", context);
        String subject = messageSource.getMessage("email.reset.title", null, locale);
        sendEmail(user.email, subject, content, false, true);
    }
}
