package org.radarbase.management.web.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.springframework.http.MediaType
import org.springframework.test.context.transaction.TestTransaction
import java.io.IOException
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

/**
 * Utility class for testing REST controllers.
 */
object TestUtil {
    /* MediaType for JSON UTF8 */
    val APPLICATION_JSON_UTF8 = MediaType(
        MediaType.APPLICATION_JSON.type,
        MediaType.APPLICATION_JSON.subtype, Charset.forName("utf8")
    )
    val APPLICATION_JSON_PATCH = MediaType(
        "application",
        "json-patch+json", Charset.forName("utf8")
    )
    private val module = JavaTimeModule()
    private val mapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .registerModule(module)

    /**
     * Convert a JSON String to an object.
     *
     * @param json JSON String to convert.
     * @param  objectClass Object class to form.
     *
     * @return the converted object instance.
     */
    @Throws(IOException::class)
    fun <T : Any> convertJsonStringToObject(json: String?, objectClass: Class<T>?): Any {
        return mapper.readValue(json, objectClass)
    }

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert
     * @return the JSON byte array
     */
    @Throws(IOException::class)
    fun convertObjectToJsonBytes(`object`: Any?): ByteArray {
        return mapper.writeValueAsBytes(`object`)
    }

    /**
     * Create a byte array with a specific size filled with specified data.
     *
     * @param size the size of the byte array
     * @param data the data to put in the byte array
     * @return the JSON byte array
     */
    fun createByteArray(size: Int, data: String): ByteArray {
        val byteArray = ByteArray(size)
        for (i in 0 until size) {
            byteArray[i] = data.toByte(2)
        }
        return byteArray
    }

    /**
     * Creates a matcher that matches when the examined string reprensents the same instant as the
     * reference datetime.
     *
     * @param date the reference datetime against which the examined string is checked
     */
    fun sameInstant(date: ZonedDateTime): ZonedDateTimeMatcher {
        return ZonedDateTimeMatcher(date)
    }

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    @Throws(Exception::class)
    fun equalsVerifier(clazz: Class<*>): Boolean {
        val domainObject1 = clazz.getConstructor().newInstance()
        Assertions.assertThat(domainObject1.toString()).isNotNull()
        Assertions.assertThat(domainObject1).isEqualTo(domainObject1)
        Assertions.assertThat(domainObject1.hashCode()).isEqualTo(domainObject1.hashCode())
        // Test with an instance of another class
        val testOtherObject = Any()
        Assertions.assertThat(domainObject1).isNotEqualTo(testOtherObject)
        // Test with an instance of the same class
        val domainObject2 = clazz.getConstructor().newInstance()
        Assertions.assertThat(domainObject1).isNotEqualTo(domainObject2)
        // HashCodes are equals because the objects are not persisted yet
        Assertions.assertThat(domainObject1.hashCode()).isEqualTo(domainObject2.hashCode())
        return true
    }

    /**
     * This allows to commit current transaction and start a new transaction.
     */
    fun commitTransactionAndStartNew() {
        // flag this transaction for commit and end it
        TestTransaction.flagForCommit()
        TestTransaction.end()
        TestTransaction.start()
        TestTransaction.flagForCommit()
    }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference
     * datetime.
     */
    class ZonedDateTimeMatcher(private val date: ZonedDateTime) : TypeSafeDiagnosingMatcher<String?>() {
        override fun matchesSafely(item: String?, mismatchDescription: Description): Boolean {
            return try {
                if (!date.isEqual(ZonedDateTime.parse(item))) {
                    mismatchDescription.appendText("was ").appendValue(item)
                    return false
                }
                true
            } catch (e: DateTimeParseException) {
                mismatchDescription.appendText("was ").appendValue(item)
                    .appendText(", which could not be parsed as a ZonedDateTime")
                false
            }
        }

        override fun describeTo(description: Description) {
            description.appendText("a String representing the same Instant as ").appendValue(date)
        }
    }
}
