package org.lab.mars.onem2m.MiddleWare.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.List;

import org.apache.zookeeper.server.NettyServerCnxn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketServerChannelHandler extends
        SimpleChannelInboundHandler<Object> {
    private static Logger LOG = LoggerFactory
            .getLogger(PacketServerChannelHandler.class);

    private List<String> servers;

    public PacketServerChannelHandler(List<String> server) {
        this.servers = server;

    }

    private static final AttributeKey<NettyServerCnxn> STATE = AttributeKey
            .valueOf("MyHandler.nettyServerCnxn");

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        ctx.fireChannelRegistered();

    };

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.info("Channel disconnect caused close:{}", cause);

        ctx.close();
    }

    /*
     * 将server拆分为ip以及port
     */
    private String[] spilitString(String ip) {
        String[] splitMessage = ip.split(":");
        return splitMessage;
    }

}
