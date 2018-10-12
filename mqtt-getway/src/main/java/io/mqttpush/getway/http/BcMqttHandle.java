package io.mqttpush.getway.http;

import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.http.controller.ControllBeans;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 处理BC后端发过来的报文 接到报文并且丢给ab 端
 * 
 * @author tianzhenjiu
 *
 */
public class BcMqttHandle extends ChannelInboundHandlerAdapter {

	Logger logger = Logger.getLogger(getClass());

	Map<String, Channel> bcChannels;

	final GetWayConstantBean constantBean = GetWayConstantBean.instance();

	public BcMqttHandle() {
		super();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof MqttConnAckMessage) {

			MqttConnAckMessage ackMessage = (MqttConnAckMessage) msg;

			switch (ackMessage.variableHeader().connectReturnCode()) {
			case CONNECTION_ACCEPTED:
				processLoginSuccess(ctx.channel());
				break;
			default:
				break;
			}
		} else if(msg instanceof MqttPublishMessage) {

			String callback = ctx.channel().attr(constantBean.bcHttpCallBackAttr).get();

			if (callback != null) {
				final DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
						HttpMethod.POST, callback);

				ChannelFuture channelFuture = constantBean.bootstrap.connect(constantBean.mqttserver,
						constantBean.mqttport);
				channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							future.channel().writeAndFlush(defaultFullHttpRequest)
									.addListener(ChannelFutureListener.CLOSE);
						}
					}
				});
			}

		}
		else {
			ctx.fireChannelRead(msg);
		}

	}
	
	
	/**
	 * 处理登录成功，发送缓存的消息
	 * @param channel
	 */
	public void processLoginSuccess(Channel channel) {
		
		channel.attr(ControllBeans.loginKey).set(true);
		
		MqttPublishMessage mqttPublishMessages=null;
		
		while((mqttPublishMessages=ControllBeans.mqttPublishMessages.poll())!=null) {
			channel.write(mqttPublishMessages);
		}
		
		channel.flush();
	}

}
