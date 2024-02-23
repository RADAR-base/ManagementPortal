package org.radarbase.management.web.rest.errors

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A base parameterized exception, which can be translated on the client side.
 *
 *
 * For example:
 *
 *
 * `throw new RadarWebApplicationException("Message to client", "entity",
 * "error.errorCode")`
 *
 *
 * can be translated with:
 *
 *
 * `"error.myCustomError" : "The server says {{param0}} to {{param1}}"`
 */
open class RadarWebApplicationException @JvmOverloads constructor(
    status: HttpStatus?, message: String?, entityName: String,
    errorCode: String?, params: Map<String, String?>? = emptyMap<String, String>()
) : ResponseStatusException(status, message, null) {
    override val message: String?
    val entityName: String
    val errorCode: String?
    private val paramMap: MutableMap<String, String?> = HashMap()
    /**
     * A base parameterized exception, which can be translated on the client side.
     * @param status [HttpStatus] code.
     * @param message message to client.
     * @param entityName entityRelated from [EntityName]
     * @param errorCode errorCode from [ErrorConstants]
     * @param params map of optional information.
     */
    /**
     * Create an exception with the given parameters. This will be used to to create response
     * body of the request.
     *
     * @param message    Error message to the client
     * @param entityName Entity related to the exception
     * @param errorCode  error code defined in MP if relevant.
     */
    init {
        // add default timestamp first, so a timestamp key in the paramMap will overwrite it
        paramMap["timestamp"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .format(Date())
        paramMap.putAll(params!!)
        this.message = message
        this.entityName = entityName
        this.errorCode = errorCode
    }

    val exceptionVM: RadarWebApplicationExceptionVM
        get() = RadarWebApplicationExceptionVM(message, entityName, errorCode, paramMap)
}
