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
    lateinit var login: @Pattern(regexp = "^[_'.@A-Za-z0-9- ]*$") @Size(max = 50, min = 1) String
    var firstName: @Size(max = 50) String? = null
    var lastName: @Size(max = 50) String? = null
    var email: @Email @Size(min = 5, max = 100) String? = null
    var isActivated = false
    var langKey: @Size(min = 2, max = 5) String? = null
    var createdBy: String? = null
    var createdDate: ZonedDateTime? = null
    var lastModifiedBy: String? = null
    var lastModifiedDate: ZonedDateTime? = null
    var roles: Set<RoleDTO>? = null
    var authorities: Set<String>? = null
    var accessToken: String? = null
    override fun toString(): String {
        return ("UserDTO{"
                + "login='" + login + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + ", activated=" + isActivated
                + ", langKey='" + langKey + '\''
                + ", createdBy=" + createdBy
                + ", createdDate=" + createdDate
                + ", lastModifiedBy='" + lastModifiedBy + '\''
                + ", lastModifiedDate=" + lastModifiedDate
                + "}")
    }
}
