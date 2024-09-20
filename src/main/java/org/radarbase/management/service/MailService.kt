package org.radarbase.management.service

import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*

/**
 * Service for sending emails.
 *
 * We use the @Async annotation to send emails asynchronously.
 */
@Service
class MailService(
    @Autowired private val managementPortalProperties: ManagementPortalProperties,
    @Autowired private val javaMailSender: JavaMailSender,
    @Autowired private val messageSource: MessageSource,
    @Autowired private val templateEngine: SpringTemplateEngine,
) {
    /**
     * Send an email.
     * @param to email address to send to
     * @param subject subject line
     * @param content email contents
     * @param isMultipart send as multipart
     * @param isHtml send as html
     */
    @Async
    fun sendEmail(
        to: String?,
        subject: String?,
        content: String?,
        isMultipart: Boolean,
        isHtml: Boolean,
    ) {
        log.debug(
            "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart,
            isHtml,
            to,
            subject,
            content,
        )

        // Prepare message using a Spring helper
        val mimeMessage = javaMailSender.createMimeMessage()
        try {
            val message =
                MimeMessageHelper(
                    mimeMessage,
                    isMultipart,
                    StandardCharsets.UTF_8.name(),
                )
            message.setTo(to!!)
            message.setFrom(managementPortalProperties.mail.from)
            message.setSubject(subject!!)
            message.setText(content!!, isHtml)
            javaMailSender.send(mimeMessage)
            log.debug("Sent email to User '{}'", to)
        } catch (e: Exception) {
            log.warn("Email could not be sent to user '{}'", to, e)
        }
    }

    /**
     * Send an account activation email to a given user.
     * @param user the user to send to
     */
    @Async
    fun sendActivationEmail(user: User) {
        log.debug("Sending activation email to '{}'", user.email)
        val locale = Locale.forLanguageTag(user.langKey)
        val context = Context(locale)
        context.setVariable(USER, user)
        context.setVariable(
            BASE_URL,
            managementPortalProperties.common.managementPortalBaseUrl,
        )
        val content = templateEngine.process("activationEmail", context)
        val subject = messageSource.getMessage("email.activation.title", null, locale)
        sendEmail(user.email, subject, content, isMultipart = false, isHtml = true)
    }

    /**
     * Send account creation email to a given user.
     * @param user the user
     */
    @Async
    fun sendCreationEmail(
        user: User,
        duration: Long,
    ) {
        log.debug("Sending creation email to '{}'", user.email)
        val locale = Locale.forLanguageTag(user.langKey)
        val context = Context(locale)
        context.setVariable(USER, user)
        context.setVariable(
            BASE_URL,
            managementPortalProperties.common.managementPortalBaseUrl,
        )
        context.setVariable(EXPIRY, Duration.ofSeconds(duration).toHours())
        val content = templateEngine.process("creationEmail", context)
        val subject = messageSource.getMessage("email.activation.title", null, locale)
        sendEmail(user.email, subject, content, isMultipart = false, isHtml = true)
    }

    /**
     * Send account creation email for a user to an address different than the users' address.
     * @param user the created user
     * @param email the address to send to
     */
    @Async
    fun sendCreationEmailForGivenEmail(
        user: User,
        email: String?,
    ) {
        log.debug("Sending creation email to '{}'", email)
        val locale = Locale.forLanguageTag(user.langKey)
        val context = Context(locale)
        context.setVariable(USER, user)
        context.setVariable(
            BASE_URL,
            managementPortalProperties.common.managementPortalBaseUrl,
        )
        val content = templateEngine.process("creationEmail", context)
        val subject = messageSource.getMessage("email.activation.title", null, locale)
        sendEmail(email, subject, content, false, true)
    }

    /**
     * Send a password reset email to a given user.
     * @param user the user
     */
    @Async
    fun sendPasswordResetMail(user: User) {
        log.debug("Sending password reset email to '{}'", user.email)
        val locale = Locale.forLanguageTag(user.langKey)
        val context = Context(locale)
        context.setVariable(USER, user)
        context.setVariable(
            BASE_URL,
            managementPortalProperties.common.managementPortalBaseUrl,
        )
        val content = templateEngine.process("passwordResetEmail", context)
        val subject = messageSource.getMessage("email.reset.title", null, locale)
        sendEmail(user.email, subject, content, false, true)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MailService::class.java)
        private const val USER = "user"
        private const val BASE_URL = "baseUrl"
        private const val EXPIRY = "expiry"
    }
}
