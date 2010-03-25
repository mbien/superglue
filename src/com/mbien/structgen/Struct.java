/*
 * Created on Tuesday, March 23 2010 at 18:45
 */
package com.mbien.structgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * @author Michael Bien
 */
@Target(value = {ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface Struct {
    String header();

    /**
     * The name of the struct.
     */
    String name() default "_default_";
}
