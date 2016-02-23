package org.lab.mars.onem2m.MiddleWare.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.zookeeper.KeeperException;
import org.lab.mars.onem2m.MiddleWare.network.TcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketClientChannelHandler extends
        SimpleChannelInboundHandler<Object> {

    private static final Logger LOG = LoggerFactory
            .getLogger(PacketClientChannelHandler.class);
    private TcpClient tcpClient;

    public PacketClientChannelHandler(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    public PacketClientChannelHandler() throws KeeperException,
            InterruptedException {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        LOG.info("close ctx,because of:{}", cause);
        ctx.close();
    }
}