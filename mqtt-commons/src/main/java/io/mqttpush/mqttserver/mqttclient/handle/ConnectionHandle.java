package io.mqttpush.mqttserver.mqttclient.handle;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.mqttclient.service.ApiService;
import io.mqttpush.mqttserver.mqttclient.service.DefaultApiService;
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

	public ConnectionHandle(ApiService apiService, String deviceId, String username, String password, String substop) {
		super();
		this.deviceId = deviceId;
		this.username = username;
		this.password = password;
		this.substop = substop;
		this.apiService = (apiService == null) ? DefaultApiService.intanceof() : apiService;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		apiService.setChannel(ctx.channel());
		apiService.login(deviceId, username, password);
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
			case DISCONNECT:
				ctx.close();
				break;
			default:
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
