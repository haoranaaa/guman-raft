package com.guman.raft.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author duanhaoran
 * @since 2020/4/19 11:41 PM
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface AcceptHandler {

    AcceptType value();

}
