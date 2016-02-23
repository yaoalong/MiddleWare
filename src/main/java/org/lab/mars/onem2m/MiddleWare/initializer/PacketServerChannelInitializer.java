package org.lab.mars.onem2m.MiddleWare.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.List;

import org.lab.mars.onem2m.MiddleWare.handler.PacketServerChannelHandler;

public class PacketServerChannelInitializer extends
        ChannelInitializer<SocketChannel> {
    private List<String> servers;

    public PacketServerChannelInitializer(List<String> servers) {
        this.servers = servers;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast(new ObjectEncoder());
        channelPipeline.addLast(new ObjectDecoder(ClassResolvers
                .cacheDisabled(null)));
        channelPipeline.addLast(new PacketServerChannelHandler(servers));
    }
}