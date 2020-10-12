package com.guman.raft.http.codec.handler;

import com.guman.raft.common.util.AnnotatedFindUtil;
import com.guman.raft.http.annotation.AcceptHandler;
import com.guman.raft.http.annotation.AcceptType;
import com.guman.raft.http.api.AcceptMessageHandler;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

/**
 * @author duanhaoran
 * @since 2020/4/20 4:15 PM
 */
public abstract class AbstractAcceptMsgAdaptive<T> extends SimpleChannelInboundHandler<T> {

    protected AcceptMessageHandler acceptMessageHandler;

    protected AcceptMessageHandler getAcceptMsgAdaptive() {
        if (acceptMessageHandler == null) {
            synchronized (AcceptMessageHandler.class){
                if (acceptMessageHandler == null) {
                    acceptMessageHandler = initAdaptive();
                }
            }
        }
        return acceptMessageHandler;
    }

    private AcceptMessageHandler initAdaptive() {
        Set<Class<?>> typeWithAnnotated = AnnotatedFindUtil.findTypeWithAnnotated(AcceptHandler.class);
        for (Class<?> adpative : typeWithAnnotated) {
            AcceptHandler annotation = adpative.getAnnotation(AcceptHandler.class);
            if (annotation.value() == getAcceptType()) {
                try {
                    return (AcceptMessageHandler) adpative.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    return null;
                }
            }
        }
        return null;
    }

    protected abstract AcceptType getAcceptType();

}
