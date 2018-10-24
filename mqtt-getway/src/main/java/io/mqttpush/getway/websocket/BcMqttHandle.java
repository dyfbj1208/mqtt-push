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

	 
	 public BcMqttHandle(Function<Channel, Channel> getBcChannel,
						 Function<Channel, Channel> getAbChannel) {
		super();
		this.getBcChannel = getBcChannel;
		this.getAbChannel = getAbChannel;
	}

	

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {


		 Channel abchannel=getAbChannel.apply(ctx.channel());

		 /**
		  * 关闭前端连接，触发AbWebSocketHandler的
		  * aBChannelClose 处理，使连接计数器准确
		  */
		 abchannel.close();

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
