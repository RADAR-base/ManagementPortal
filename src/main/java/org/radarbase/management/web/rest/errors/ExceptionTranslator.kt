package org.radarbase.management.web.rest.errors

import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.provider.NoSuchClientException
import org.springframework.transaction.TransactionSystemException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 */
@ControllerAdvice
class ExceptionTranslator {
    /**
     * Translate a [ConcurrencyFailureException].
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(ConcurrencyFailureException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    fun processConcurrencyError(ex: ConcurrencyFailureException?): ErrorVM =
        ErrorVM(ErrorConstants.ERR_CONCURRENCY_FAILURE)

    /**
     * Translate a [TransactionSystemException].
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(TransactionSystemException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processValidationError(ex: TransactionSystemException): ErrorVM {
        // ConstraintValidationException occurs. Need to investigate what other exceptions result
        // in this one and probably add a check for it.
        return ErrorVM(ErrorConstants.ERR_VALIDATION, ex.message)
    }

    /**
     * Translate a [MethodArgumentNotValidException].
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processValidationError(ex: MethodArgumentNotValidException): ErrorVM {
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors
        val dto = ErrorVM(ErrorConstants.ERR_VALIDATION)
        for (fieldError in fieldErrors) {
            dto.add(
                fieldError.objectName,
                fieldError.field,
                (fieldError.code?.plus(": ") ?: "undefined.error.code") + fieldError.defaultMessage,
            )
        }
        return dto
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseBody
    fun processValidationError(ex: MethodArgumentTypeMismatchException): ErrorVM =
        ErrorVM(
            ErrorConstants.ERR_VALIDATION,
            ex.name + ": " + ex.message,
        )

    @ExceptionHandler(RadarWebApplicationException::class)
    fun processParameterizedValidationError(ex: RadarWebApplicationException): ResponseEntity<RadarWebApplicationExceptionVM?> =
        processRadarWebApplicationException(ex)

    @ExceptionHandler(NotFoundException::class)
    fun processNotFound(ex: NotFoundException): ResponseEntity<RadarWebApplicationExceptionVM?> =
        processRadarWebApplicationException(ex)

    @ExceptionHandler(InvalidStateException::class)
    fun processNotFound(ex: InvalidStateException): ResponseEntity<RadarWebApplicationExceptionVM?> =
        processRadarWebApplicationException(ex)

    @ExceptionHandler(RequestGoneException::class)
    fun processNotFound(ex: RequestGoneException): ResponseEntity<RadarWebApplicationExceptionVM?> =
        processRadarWebApplicationException(ex)

    @ExceptionHandler(BadRequestException::class)
    fun processNotFound(ex: BadRequestException): ResponseEntity<RadarWebApplicationExceptionVM?> =
        processRadarWebApplicationException(ex)

    @ExceptionHandler(InvalidRequestException::class)
    fun processNotFound(ex: InvalidRequestException): ResponseEntity<RadarWebApplicationExceptionVM?> =
        processRadarWebApplicationException(ex)

    /**
     * Translate a [ConflictException].
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(ConflictException::class)
    fun processConflict(ex: ConflictException): ResponseEntity<RadarWebApplicationExceptionVM?> =
        processRadarWebApplicationException(ex)

    private fun processRadarWebApplicationException(
        exception: RadarWebApplicationException,
    ): ResponseEntity<RadarWebApplicationExceptionVM?> =
        ResponseEntity
            .status(exception.status)
            .headers(
                HeaderUtil.createExceptionAlert(
                    exception.entityName,
                    exception.errorCode,
                    exception.message,
                ),
            ).body(exception.exceptionVM)

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    fun processAccessDeniedException(e: AccessDeniedException): ErrorVM =
        ErrorVM(ErrorConstants.ERR_ACCESS_DENIED, e.message)

    @ExceptionHandler(NotAuthorizedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    fun processRadarNotAuthorizedException(e: NotAuthorizedException): ErrorVM =
        ErrorVM(ErrorConstants.ERR_ACCESS_DENIED, e.message)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun processMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): ErrorVM =
        ErrorVM(ErrorConstants.ERR_METHOD_NOT_SUPPORTED, ex.message)

    /**
     * If a client tries to initiate an OAuth flow with a non-existing client, this will
     * translate the error into a bad request status. Otherwise we return an internal server
     * error status, but it is not a server error.
     *
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(NoSuchClientException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun processNoSuchClientException(ex: NoSuchClientException): ErrorVM =
        ErrorVM(ErrorConstants.ERR_NO_SUCH_CLIENT, ex.message)

    @ExceptionHandler(ResponseStatusException::class)
    fun responseStatusResponse(ex: ResponseStatusException): ResponseEntity<ErrorVM> =
        ResponseEntity
            .status(ex.status)
            .body(ErrorVM(null, ex.message))

    /**
     * Generic exception translator.
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(Exception::class)
    fun processRuntimeException(ex: Exception): ResponseEntity<ErrorVM> {
        val builder: ResponseEntity.BodyBuilder
        val errorVm: ErrorVM
        logger.error("Failed to process message", ex)
        val responseStatus =
            AnnotationUtils.findAnnotation(
                ex.javaClass,
                ResponseStatus::class.java,
            )
        if (responseStatus != null) {
            builder = ResponseEntity.status(responseStatus.value)
            errorVm =
                ErrorVM(
                    "error." + responseStatus.value.value(),
                    responseStatus.reason,
                )
        } else {
            builder = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            errorVm =
                ErrorVM(
                    ErrorConstants.ERR_INTERNAL_SERVER_ERROR,
                    "Internal server error",
                )
        }
        return builder.body(errorVm)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExceptionTranslator::class.java)
    }
}
