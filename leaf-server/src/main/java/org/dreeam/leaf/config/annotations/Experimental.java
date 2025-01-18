package org.dreeam.leaf.config.annotations;

import java.lang.annotation.*;

/**
 * Indicates that a feature is experimental and may be removed or changed in the future.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface Experimental {
}
