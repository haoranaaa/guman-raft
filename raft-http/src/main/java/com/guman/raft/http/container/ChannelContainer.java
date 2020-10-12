package com.guman.raft.http.container;

import com.google.common.collect.Maps;
import com.guman.raft.common.current.RaftNameThreadFactory;
import io.netty.channel.Channel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author duanhaoran
 * @since 2020/4/15 5:34 PM
 */
public class ChannelContainer {

    private static Map<String, Channel> channelMap = Maps.newConcurrentMap();

    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new RaftNameThreadFactory("channel-check-"));

    static {
        executor.scheduleAtFixedRate(() -> {
            getAllChannel().forEach(i->{
                if (!i.isActive()) {
                    unRegister(i);
                }
            });

        }, ThreadLocalRandom.current().nextInt(10), 10, TimeUnit.SECONDS);
    }
    public static void register(Channel channel) {
        channelMap.put(channel.id().asLongText(), channel);
    }

    public static void unRegister(Channel channel) {
        channelMap.remove(channel.id().asLongText());
    }



    public static Collection<Channel> getAllChannel() {
        return channelMap.values();
    }

    public static Channel getChannelById(String id) {
        return channelMap.get(id);
    }


}
