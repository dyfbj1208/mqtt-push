package io.mqttpush.mqttclient.handle;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import io.mqttpush.mqttclient.service.ApiService;
import io.mqttpush.mqttclient.service.DefaultApiService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;

/**
 * 
 * @author tianzhenjiu
 *
 */
public class ConnectionHandle extends ChannelInboundHandlerAdapter {

	Logger logger = Logger.getLogger(getClass());

	String username;
	String password;
	String deviceId;
	ApiService apiService;
	String substop;
	AtomicBoolean isValidate;

	public ConnectionHandle(AtomicBoolean isValidate,ApiService apiService, String deviceId, String username, String password, String substop) {
		super();
		this.isValidate=isValidate;
		this.deviceId = deviceId;
		this.username = username;
		this.password = password;
		this.substop = substop;
		this.apiService = (apiService == null) ? DefaultApiService.intance() : apiService;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		apiService.setChannel(ctx.channel());
		apiService.login(deviceId, username, password);
	}

	@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			// TODO Auto-generated method stub
			super.exceptionCaught(ctx, cause);
		}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof MqttMessage) {

			MqttMessage message = (MqttMessage) msg;
			MqttFixedHeader fixedHeader = message.fixedHeader();
			MqttMessageType messageType = fixedHeader.messageType();

			switch (messageType) {
			case CONNACK:
				ack(ctx, (MqttConnAckMessage) message);
				break;
			case PINGRESP://如果又心跳回复就置为可用
				isValidate.compareAndSet(false, true);
				break;
			case DISCONNECT:
				ctx.close();
				break;
			default://如果有消息来了就置为可用,因为这里的default匹配的一定是其publish 或sub消息类型
				isValidate.compareAndSet(false, true);
				ctx.fireChannelRead(msg);
				break;
			}
		} else
			ctx.close();

	}

	public void ack(ChannelHandlerContext ctx, MqttConnAckMessage ackMessage) {

		switch (ackMessage.variableHeader().connectReturnCode()) {

		case CONNECTION_ACCEPTED:

			AttributeKey<Boolean> loginKey = AttributeKey.valueOf("login");
			final Channel channel = ctx.channel();
			channel.attr(loginKey).set(true);
			// 登录成功
			apiService.subscribe(substop, MqttQoS.AT_LEAST_ONCE);
			break;
		default:
			if(logger.isDebugEnabled()) {
				// 登录失败
				logger.warn("登录失败"+ackMessage.variableHeader().connectReturnCode());
			}
			break;
		}
	}

}
