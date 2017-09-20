package org.radarcns.auth.annotation;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for indicating a resource as being secured. Can be used to annotate classes and
 * methods.  Annotation on a class will cause the security to be applied to all methods defined
 * in the class. Implementations must make sure that annotations on methods will override the ones
 * defined on class-level, they are not additive.
 */

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured {
    /**
     * Array of strings listing the token scopes that are allowed to access this resource
     */
    String[] scopesAllowed() default {};
}
