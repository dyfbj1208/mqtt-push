package io.netty.proxy;

import java.util.Map;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.proxy.WebSocketHandler;

public class ClientHandle extends ChannelInboundHandlerAdapter {

	
	Logger logger=Logger.getLogger(getClass());

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		 Map<Channel, Channel>  bc2abChannels=WebSocketHandler.bc2abChannels;
		 Channel channel=ctx.channel();
		 if(bc2abChannels.containsKey(channel)) {
			 bc2abChannels.get(channel).close();
		 }
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		 Map<Channel, Channel>  bc2abChannels=WebSocketHandler.bc2abChannels;
		 Channel channel=ctx.channel();
		 if(bc2abChannels.containsKey(channel)) {
			 
			 
			 ByteBuf buf=(ByteBuf)msg;
			 bc2abChannels.get(channel).writeAndFlush(new BinaryWebSocketFrame(buf));
			 logger.info("写MQTT 到websocket");
		 }
	}

	

	
}
