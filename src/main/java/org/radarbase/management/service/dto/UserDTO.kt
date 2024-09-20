package org.radarbase.management.service.dto

import org.hibernate.validator.constraints.Email
import java.time.ZonedDateTime
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * A DTO representing a user, with his authorities.
 */
open class UserDTO {
    var id: Long? = null

    @Pattern(regexp = "^[_'.@A-Za-z0-9- ]*$")
    @Size(max = 50, min = 1)
    var login: String? = null

    @Size(max = 50)
    var firstName: String? = null

    @Size(max = 50)
    var lastName: String? = null

    @Email
    @Size(min = 5, max = 100)
    var email: String? = null
    var isActivated = false

    @Size(min = 2, max = 5)
    var langKey: String? = null
    var createdBy: String? = null
    var createdDate: ZonedDateTime? = null
    var lastModifiedBy: String? = null
    var lastModifiedDate: ZonedDateTime? = null
    var roles: Set<RoleDTO>? = null
    var authorities: Set<String>? = null
    var accessToken: String? = null

    /** Identifier for association with the identity service provider.
     * Null if not linked to an external identity. */
    var identity: String? = null

    override fun toString(): String =
        (
            "UserDTO{" +
                "login='" + login + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", activated=" + isActivated +
                ", langKey='" + langKey + '\'' +
                ", createdBy=" + createdBy +
                ", createdDate=" + createdDate +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                "}"
            )
}
