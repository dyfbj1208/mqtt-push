package io.mqttpush.mqttserver.handle;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.netty.channel.Channel;
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

	int defaultwaittime=10;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	
		final Channel channel=ctx.channel();
		
		channel.eventLoop().schedule(()->{
			/**
			 * 如果channel 没有登录就关掉channel
			 */
			if(!channel.hasAttr(ConstantBean.loginKey)
					||(!channel.attr(ConstantBean.loginKey).get())) {				
				channel.close();
				if(logger.isDebugEnabled()) {
					logger.debug("关闭未登录的超时channel");
				}
			}
		}, defaultwaittime, TimeUnit.SECONDS);
	}


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
