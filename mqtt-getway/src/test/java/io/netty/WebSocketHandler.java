package io.netty;

import java.time.LocalDateTime;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.mqtt.MqttDecoder;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("用户上线: " + ctx.channel().id().asLongText());

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		System.out.println("用户下线: " + ctx.channel().id().asLongText());
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (!(msg instanceof BinaryWebSocketFrame)) {
			ctx.channel().writeAndFlush(new TextWebSocketFrame("来自服务端: 不支持" + LocalDateTime.now()));
			return;
		}
		
		
		
		BinaryWebSocketFrame binaryWebSocketFrame=(BinaryWebSocketFrame)msg;
		ctx.fireChannelRead(binaryWebSocketFrame.content());
//		ctx.fireChannelRead(msg);

//		Channel channel = ctx.channel();
//		System.out.println(msg);
//		MqttDecoder decoder = new MqttDecoder();
//		decoder.channelRead(ctx, msg);
//		System.out.println(channel.remoteAddress() + ": " + msg);
//
//		ctx.channel().writeAndFlush(new TextWebSocketFrame("来自服务端: " + LocalDateTime.now()));

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}
}