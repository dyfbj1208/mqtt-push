package io.mqttpush.mqttserver.handle;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * 抽象的channelhandle
 * 提供了基本的操作
 * @author tzj
 *
 */
public abstract  class AbstractHandle extends ChannelInboundHandlerAdapter implements Handle{

	
	Logger logger = Logger.getLogger(getClass());

	
	
	@Override
	public void connec(ChannelHandlerContext context) {
		try {
			super.channelActive(context);
		} catch (Exception e) {
			logger.warn("异常",e);
		}
	}

  
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		onMessage(ctx,(MqttMessage)msg);
	}


	@Override
	public abstract void onMessage(ChannelHandlerContext context, MqttMessage message) ;


	@Override
	public  void disconnect(ChannelHandlerContext context) {
		context.close();
	}
	
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		disconnect(ctx);
		super.channelInactive(ctx);

	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

		logger.debug("异常 " + ctx.channel(), cause);
		disconnect(ctx);
	}
}
