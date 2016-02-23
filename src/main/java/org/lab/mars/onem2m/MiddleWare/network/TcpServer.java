package org.lab.mars.onem2m.MiddleWare.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lab.mars.onem2m.MiddleWare.initializer.PacketServerChannelInitializer;

public class TcpServer {
    private Set<Channel> channels;
    private List<String> servers;

    public TcpServer(List<String> servers) {
        this.servers = servers;
        this.channels = new HashSet<Channel>();
    }

    public void bind(String host, int port) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(NetworkEventLoopGroup.bossGroup,
                NetworkEventLoopGroup.workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1000)
                .childHandler(new PacketServerChannelInitializer(servers));
        b.bind(host, port).addListener((ChannelFuture channelFuture) -> {
            channels.add(channelFuture.channel());
        });
    }

    public void close() {
        for (Channel channel : channels) {
            channel.close();
        }
    }

    public static void main(String args[]) throws InterruptedException {
        TcpServer tcpServer = new TcpServer(null);
        tcpServer.bind("localhost", 2182);
    }
}
