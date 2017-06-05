package com.matsuz.gdns;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.xbill.DNS.*;


public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message request;
        try {
            request = new Message(packet.content().nioBuffer());
        } catch (WireParseException e) {
            // TODO: LOG
            return;
        }

        Record question = request.getQuestion();
        if (question == null) return;
        Message response = Resolver.resolve(
                question.getName(),
                question.getType(),
                packet.sender().getAddress().getHostAddress());
        response.getHeader().setID(request.getHeader().getID());
        response.getHeader().setFlag(Flags.QR);
        response.addRecord(request.getQuestion(), Section.QUESTION);

        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response.toWire()), packet.sender()));
    }
}
