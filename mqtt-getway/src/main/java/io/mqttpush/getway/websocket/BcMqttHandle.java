package io.mqttpush.getway.websocket;

import io.mqttpush.getway.common.Statistics;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.apache.log4j.Logger;

import java.util.function.Function;

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

	Function<Channel, ChannelFuture> closeChannelFunc;
	 
	 public BcMqttHandle(Function<Channel, Channel> getBcChannel,
						 Function<Channel, Channel> getAbChannel,Function<Channel, ChannelFuture> closeChannelFunc) {
		super();
		this.getBcChannel = getBcChannel;
		this.getAbChannel = getAbChannel;
		this.closeChannelFunc=closeChannelFunc;
	}

	

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {


		 Channel abchannel=getAbChannel.apply(ctx.channel());

		 if(abchannel!=null){
			 closeChannelFunc.apply(abchannel);
		 }


		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		
		 Channel bcchannel=ctx.channel();
		 Channel abchannel=getAbChannel.apply(bcchannel);
		 
		 if(abchannel!=null) {
			 abchannel.writeAndFlush(new BinaryWebSocketFrame((ByteBuf) msg));
			 Statistics.responseCount.incrementAndGet();
			 if(logger.isDebugEnabled()) {
				 logger.debug("写MQTT 到websocket");
			 }
		 }
		 
	}

	 
}
