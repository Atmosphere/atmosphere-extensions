/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atmosphere.extensions.gwt.jersey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author p.havelaar
 */
@Target(value = {ElementType.PARAMETER,ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface GwtPayload {
}
