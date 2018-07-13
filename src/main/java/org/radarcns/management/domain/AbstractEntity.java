package org.radarcns.management.domain;

import org.radarcns.management.domain.support.AbstractEntityListener;

import java.io.Serializable;

/**
 * Base abstract class for entities which will hold definitions for created, last modified by and
 * created, last modified by date. These will be populated by {@link AbstractEntityListener} on
 * the {@code PostLoad} trigger. Since this class is not an Entity or a MappedSuperClass, we need
 * to define the entitylistener on each of the subclasses.
 */
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract Long getId();
}
