package org.lab.mars.onem2m.MiddleWare.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import org.lab.mars.onem2m.MiddleWare.handler.PacketClientChannelHandler;
import org.lab.mars.onem2m.MiddleWare.network.TcpClient;

public class PacketClientChannelInitializer extends
        ChannelInitializer<SocketChannel> {
    private TcpClient tcpClient;

    public PacketClientChannelInitializer(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast(new ObjectEncoder());
        channelPipeline.addLast(new ObjectDecoder(ClassResolvers
                .cacheDisabled(null)));
        channelPipeline.addLast(new PacketClientChannelHandler(tcpClient));
    }
}
