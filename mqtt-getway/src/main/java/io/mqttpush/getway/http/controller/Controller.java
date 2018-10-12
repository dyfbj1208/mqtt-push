package io.mqttpush.getway.http.controller;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.http.BcMqttHandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;

/**
 * 抽象的controller
 * 
 * @author tianzhenjiu
 *
 */
public abstract class Controller {

	/**
	 * 标志是否初始化了channnel
	 */
	static AtomicBoolean isInit = new AtomicBoolean(false);

	final GetWayConstantBean constantBean = GetWayConstantBean.instance();

	/**
	 * 服务于请求
	 * 
	 * @param requestChannel
	 * @param request
	 * @param response
	 */
	public abstract void service(Channel requestChannel, HttpRequest request, HttpResponse response);

	/**
	 * 得到设置好了的channel，否则连接并且设置它
	 * 
	 * @param identify
	 * @param callback
	 * @return
	 */
	protected Channel getAndSetChannelByIdentify(String identify, String callback) {

		Map<String, Channel> bcHttpChannels = constantBean.bcHttpChannels;

		boolean needInitChannel = false;
		Channel channel = null;
		if (!bcHttpChannels.containsKey(identify)) {
			needInitChannel = true;
		} else {
			channel = bcHttpChannels.get(identify);
			if (channel == null || !channel.isActive()) {
				needInitChannel = true;
			}
		}

		if (needInitChannel) {
			ChannelFuture channelFuture = constantBean.httpbootstrap.connect(constantBean.mqttserver,
					constantBean.mqttport);

			channelFuture.channel().attr(constantBean.bcHttpCallBackAttr).set(callback);

			Channel oldChannel = bcHttpChannels.put(identify, channel = channelFuture.channel());

			if (oldChannel != null) {
				oldChannel.close();
			}
			loginMqtt(channel, identify, "user", "user123456");

		}

		if (bcHttpChannels.containsKey(identify)) {
			return bcHttpChannels.get(identify);
		}
		return null;

	}

	/**
	 * 登录
	 * 
	 * @param channel
	 * @param deviceId
	 * @param username
	 * @param password
	 */
	protected void loginMqtt(Channel channel, String deviceId, String username, String password) {

		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE,
				false, 0);

		MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader(MqttVersion.MQTT_3_1.protocolName(),
				MqttVersion.MQTT_3_1.protocolLevel(), true, true, false, 0, false, false, 10);

		MqttConnectPayload payload = new MqttConnectPayload(deviceId, null, null, username, password.getBytes());
		MqttConnectMessage connectMessage = new MqttConnectMessage(mqttFixedHeader, variableHeader, payload);
		channel.writeAndFlush(connectMessage);
	}

	/**
	 * 初始化配置后端连接
	 */
	public void initBcChannel() {

		if (isInit.get()) {
			return;
		}
		
		if (!isInit.compareAndSet(false, true)) {
			return;
		}

		Bootstrap bootstrap = constantBean.httpbootstrap;
		bootstrap.group(constantBean.httpgroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(MqttEncoder.INSTANCE, new MqttDecoder());
						ch.pipeline().addLast(new BcMqttHandle());
					}

				});

	}

}
