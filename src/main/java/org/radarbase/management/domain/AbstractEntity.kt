package org.radarbase.management.domain

import java.io.Serializable

/**
 * Base abstract class for entities which will hold definitions for created, last modified by and
 * created, last modified by date. These will be populated by [AbstractEntityListener] on
 * the `PostLoad` trigger. Since this class is not an Entity or a MappedSuperClass, we need
 * to define the entitylistener on each of the subclasses.
 */
abstract class AbstractEntity : Serializable {
    abstract val id: Long?

    companion object {
        private const val serialVersionUID = 1L
    }
}
