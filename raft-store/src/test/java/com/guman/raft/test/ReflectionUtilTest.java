package com.guman.raft.test;

import com.guman.raft.common.util.AnnotatedFindUtil;
import com.guman.raft.http.annotation.AcceptHandler;

import java.util.Set;

/**
 * @author duanhaoran
 * @since 2020/4/20 2:34 PM
 */
public class ReflectionUtilTest {

    public static void main(String[] args) {
        Set<Class<?>> typeWithAnnotated = AnnotatedFindUtil.findTypeWithAnnotated(AcceptHandler.class);
        typeWithAnnotated.forEach(i->System.out.println(i.getSimpleName()));
    }

}
