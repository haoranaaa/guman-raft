package com.guman.raft.common.util;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author duanhaoran
 * @since 2020/4/20 2:23 PM
 */
public class AnnotatedFindUtil {

    public static final String PACKAGE_NAME_PREFIX = "com.guman";

    public static Set<Class<?>> findTypeWithAnnotated(Class<? extends Annotation> annotation){
        Reflections ref = new Reflections(PACKAGE_NAME_PREFIX);
        return ref.getTypesAnnotatedWith(annotation);
    }
}
