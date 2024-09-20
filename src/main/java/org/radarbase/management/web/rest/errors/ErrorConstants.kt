package org.radarbase.management.web.rest.errors

object ErrorConstants {
    const val ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure"
    const val ERR_ACCESS_DENIED = "error.accessDenied"
    const val ERR_VALIDATION = "error.validation"
    const val ERR_METHOD_NOT_SUPPORTED = "error.methodNotSupported"
    const val ERR_MEDIA_TYPE_NOT_SUPPORTED = "error.mediaTypeNotSupported"
    const val ERR_INTERNAL_SERVER_ERROR = "error.internalServerError"
    const val ERR_SOURCE_TYPE_EXISTS = "error.sourceTypeExists"
    const val ERR_CLIENT_ID_EXISTS = "error.clientIdExists"
    const val ERR_OAUTH_CLIENT_PROTECTED = "error.oAuthClientProtected"
    const val ERR_OAUTH_CLIENT_ALREADY_EXISTS = "error.oAuthClientExists"
    const val ERR_OAUTH_CLIENT_ID_NOT_FOUND = "error.oAuthClientIdNotFound"
    const val ERR_SUBJECT_NOT_FOUND = "error.subjectNotFound"
    const val ERR_SOURCE_NAME_EXISTS = "error.sourceNameExists"
    const val ERR_SOURCE_NOT_FOUND = "error.sourceNotFound"
    const val ERR_SOURCE_TYPE_IN_USE = "error.sourceTypeInUse"
    const val ERR_SOURCE_TYPE_NOT_FOUND = "error.sourceTypeNotFound"
    const val ERR_GROUP_NOT_FOUND = "error.groupNotFound"
    const val ERR_GROUP_EXISTS = "error.groupExists"
    const val ERR_INVALID_AUTHORITY = "error.invalidAuthority"
    const val ERR_EMAIL_EXISTS = "error.emailexists"
    const val ERR_ORGANIZATION_NAME_NOT_FOUND = (
        "error" +
            ".organizationNameNotFound"
        )
    const val ERR_PROJECT_ID_NOT_FOUND = "error.projectIdNotFound"
    const val ERR_PROJECT_NAME_NOT_FOUND = "error.projectNameNotFound"
    const val ERR_REVISIONS_NOT_FOUND = "error.revisionsNotFound"
    const val ERR_ENTITY_NOT_FOUND = "error.entityNotFound"
    const val ERR_TOKEN_NOT_FOUND = "error.tokenNotFound"
    const val ERR_SOURCE_TYPE_NOT_PROVIDED = "error.sourceTypeNotProvided"
    const val ERR_PERSISTENT_TOKEN_DISABLED = "error.persistentTokenDisabled"
    const val ERR_ACTIVE_PARTICIPANT_PROJECT_NOT_FOUND = (
        "error" +
            ".activeParticipantProjectNotFound"
        )
    const val ERR_NO_VALID_PRIVACY_POLICY_URL_CONFIGURED = (
        "error" +
            ".noValidPrivacyPolicyUrl"
        )
    const val ERR_NO_SUCH_CLIENT = "error.noSuchClient"
    const val ERR_PROJECT_NOT_EMPTY = "error.projectNotEmpty"
    const val ERR_PASSWORD_TOO_LONG = "error.longPassword"
    const val ERR_PASSWORD_TOO_WEAK = "error.weakPassword"
    const val ERR_EMAIL_NOT_REGISTERED = "error.emailNotRegistered"
}
