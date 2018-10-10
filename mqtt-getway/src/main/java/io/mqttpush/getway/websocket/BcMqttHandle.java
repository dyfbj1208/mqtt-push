package io.mqttpush.getway.websocket;

import java.util.function.Function;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * 处理BC后端发过来的报文
 * 接到报文并且丢给ab 端
 * @author tianzhenjiu
 *
 */
public class BcMqttHandle extends ChannelInboundHandlerAdapter {

	
	Logger logger=Logger.getLogger(getClass());
	
	Function<Channel, Channel> getBcChannel;
	
	Function<Channel, Channel> getAbChannel;
	
	 
	 public BcMqttHandle(Function<Channel, Channel> getBcChannel, Function<Channel, Channel> getAbChannel) {
		super();
		this.getBcChannel = getBcChannel;
		this.getAbChannel = getAbChannel;
	}

	

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		 Channel abchannel=getAbChannel.apply(ctx.channel());
		 if(abchannel!=null&&abchannel.isActive()) {
			 abchannel.close();
		 }
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		
		 Channel bcchannel=ctx.channel();
		 Channel abchannel=getAbChannel.apply(bcchannel);
		 
		 if(abchannel!=null) {
			 abchannel.writeAndFlush(new BinaryWebSocketFrame((ByteBuf) msg));
			 
			 if(logger.isDebugEnabled()) {
				 logger.debug("写MQTT 到websocket");
			 }
		 }
		 
	}

	 
}
