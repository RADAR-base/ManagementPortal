package org.radarbase.management.web.rest.vm

import org.radarbase.management.service.dto.UserDTO
import javax.validation.constraints.Size

/**
 * View Model extending the UserDTO, which is meant to be used in the user management UI.
 */
class ManagedUserVM : UserDTO() {
    var password:
        @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
        String? = null

    override fun toString(): String = "ManagedUserVM{} " + super.toString()

    companion object {
        const val PASSWORD_MIN_LENGTH = 4
        const val PASSWORD_MAX_LENGTH = 100
    }
}
