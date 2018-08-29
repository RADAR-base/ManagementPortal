package org.radarcns.management.web.rest.errors;

import java.util.List;

import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 */
@ControllerAdvice
public class ExceptionTranslator {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionTranslator.class);

    /**
     * Translate a {@link ConcurrencyFailureException}.
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(ConcurrencyFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorVM processConcurrencyError(ConcurrencyFailureException ex) {
        return new ErrorVM(ErrorConstants.ERR_CONCURRENCY_FAILURE);
    }

    /**
     * Translate a {@link TransactionSystemException}.
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM processValidationError(TransactionSystemException ex) {
        // TODO: this is the top level exception that is thrown when e.g. a
        // ConstraintValidationException occurs. Need to investigate what other exceptions result
        // in this one and probably add a check for it.
        ErrorVM dto = new ErrorVM(ErrorConstants.ERR_VALIDATION, ex.getMessage());
        return dto;
    }

    /**
     * Translate a {@link MethodArgumentNotValidException}.
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorVM processValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        ErrorVM dto = new ErrorVM(ErrorConstants.ERR_VALIDATION);
        for (FieldError fieldError : fieldErrors) {
            dto.add(fieldError.getObjectName(), fieldError.getField(), fieldError.getCode());
        }
        return dto;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ErrorVM processValidationError(MethodArgumentTypeMismatchException ex) {
        return new ErrorVM(ErrorConstants.ERR_VALIDATION,
                ex.getName() + ": " + ex.getMessage());
    }

    @ExceptionHandler(RadarWebApplicationException.class)
    public ResponseEntity<RadarWebApplicationExceptionVM> processParameterizedValidationError(
            RadarWebApplicationException ex) {
        return processRadarWebApplicationException(ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<RadarWebApplicationExceptionVM> processNotFound(NotFoundException ex) {
        return processRadarWebApplicationException(ex);
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<RadarWebApplicationExceptionVM> processNotFound(
            InvalidStateException ex) {
        return processRadarWebApplicationException(ex);
    }

    @ExceptionHandler(RequestGoneException.class)
    public ResponseEntity<RadarWebApplicationExceptionVM> processNotFound(RequestGoneException ex) {
        return processRadarWebApplicationException(ex);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<RadarWebApplicationExceptionVM> processNotFound(BadRequestException ex) {
        return processRadarWebApplicationException(ex);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<RadarWebApplicationExceptionVM> processNotFound(
            InvalidRequestException ex) {
        return processRadarWebApplicationException(ex);
    }


    /**
     * Translate a {@link ConflictException}.
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<RadarWebApplicationExceptionVM> processConflict(
            ConflictException ex) {
        return processRadarWebApplicationException(ex);
    }

    private ResponseEntity<RadarWebApplicationExceptionVM> processRadarWebApplicationException(
            RadarWebApplicationException exception) {
        return ResponseEntity
            .status(exception.getResponse().getStatus())
            .headers(HeaderUtil.createExceptionAlert(exception.getEntityName(),
                    exception.getErrorCode(), exception.getMessage()))
            .body(exception.getExceptionVM());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorVM processAccessDeniedException(AccessDeniedException e) {
        return new ErrorVM(ErrorConstants.ERR_ACCESS_DENIED, e.getMessage());
    }

    @ExceptionHandler(NotAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorVM processRadarNotAuthorizedException(NotAuthorizedException e) {
        return new ErrorVM(ErrorConstants.ERR_ACCESS_DENIED, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorVM processMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ErrorVM(ErrorConstants.ERR_METHOD_NOT_SUPPORTED, ex.getMessage());
    }

    /**
     * If a client tries to initiate an OAuth flow with a non-existing client, this will
     * translate the error into a bad request status. Otherwise we return an internal server
     * error status, but it is not a server error.
     *
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(NoSuchClientException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorVM processNoSuchClientException(NoSuchClientException ex) {
        return new ErrorVM(ErrorConstants.ERR_NO_SUCH_CLIENT, ex.getMessage());
    }

    /**
     * Generic exception translator.
     * @param ex the exception
     * @return the view-model for the translated exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorVM> processRuntimeException(Exception ex) {
        BodyBuilder builder;
        ErrorVM errorVm;
        logger.error("Failed to process message", ex);
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(),
                ResponseStatus.class);
        if (responseStatus != null) {
            builder = ResponseEntity.status(responseStatus.value());
            errorVm = new ErrorVM("error." + responseStatus.value().value(),
                    responseStatus.reason());
        } else {
            builder = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
            errorVm = new ErrorVM(ErrorConstants.ERR_INTERNAL_SERVER_ERROR,
                    "Internal server error");
        }
        return builder.body(errorVm);
    }
}
