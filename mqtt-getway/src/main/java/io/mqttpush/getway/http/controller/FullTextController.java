package io.mqttpush.getway.http.controller;

import java.nio.charset.Charset;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.http.BcMqttHandle;
import io.mqttpush.getway.http.vo.HttpPushVo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;

/**
 * 处理JSON的fulltext
 * 
 * @author tianzhenjiu
 *
 */
public class FullTextController extends Controller {


	
	final EventLoopGroup group = new NioEventLoopGroup();

	final GetWayConstantBean constantBean = GetWayConstantBean.instance();

	protected FullTextController() {
		initBcChannel();
	}

	@Override
	public void service(HttpRequest request, HttpResponse response) {

		if (!(request instanceof FullHttpRequest)) {
			response.setStatus(HttpResponseStatus.BAD_REQUEST);
			return;
		}

		FullHttpRequest fullHttpRequest = (FullHttpRequest) request;

		ByteBuf byteBuf = fullHttpRequest.content();

		byte[] bs = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(bs);
		
		HttpPushVo httpPushVo = JSON.parseObject(new String(bs), HttpPushVo.class);
		httpPushVo.getFromIdentify();
		
		Channel channel=getChannelByIdentify(httpPushVo.getFromIdentify(), httpPushVo.getCallback());
		
		if(channel!=null) {
			
			
			MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE,
					false, 0);

			MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(
					constantBean.ONE2ONE_CHAT_PREFIX+httpPushVo.getToIdentify(),
					httpPushVo.hashCode());

			byteBuf=channel.alloc().buffer();
			byteBuf.writeCharSequence(httpPushVo.getContent(), Charset.forName("utf-8"));
			MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, variableHeader, byteBuf);
		
			
			
			if(channel.hasAttr(ControllBeans.loginKey)) {
				channel.write(mqttPublishMessage);
			}else {
				ControllBeans.mqttPublishMessages.offer(mqttPublishMessage);
			}
			
		}

	}

	private Channel getChannelByIdentify(String identify,String callback) {
		
		Map<String, Channel> bcHttpChannels=constantBean.bcHttpChannels;

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
			ChannelFuture channelFuture = 
					constantBean.bootstrap.connect(constantBean.mqttserver, constantBean.mqttport);
			
			channelFuture.channel().attr(constantBean.bcHttpCallBackAttr).set(callback);
		
			
			Channel oldChannel=bcHttpChannels.put(identify, channel=channelFuture.channel());
			
			if(oldChannel!=null) {
				oldChannel.close();
			}
			loginMqtt(channel, identify, "user", "user123456");
			
			
		}
		
		
		if(bcHttpChannels.containsKey(identify)) {
			return  bcHttpChannels.get(identify);
		}
		return  null;
		
		

	}
	
	/**
	 * 登录
	 * @param channel
	 * @param deviceId
	 * @param username
	 * @param password
	 */
	private void loginMqtt(Channel channel,String deviceId,String username,String password) {
		
		MqttFixedHeader mqttFixedHeader=new MqttFixedHeader(MqttMessageType.CONNECT,
				false, MqttQoS.AT_MOST_ONCE, false, 0);
		
		MqttConnectVariableHeader variableHeader=new MqttConnectVariableHeader(
				MqttVersion.MQTT_3_1.protocolName(), MqttVersion.MQTT_3_1.protocolLevel(), true, true, false, 0,false, false, 10);
		
		MqttConnectPayload payload=new MqttConnectPayload(deviceId, null, null, username, password.getBytes());
		MqttConnectMessage connectMessage=new MqttConnectMessage(mqttFixedHeader, variableHeader, payload);
		 channel.writeAndFlush(connectMessage);
	}

	/**
	 * 初始化配置后端连接
	 */
	public void initBcChannel() {

		Bootstrap bootstrap = constantBean.bootstrap;
		bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(MqttEncoder.INSTANCE, new MqttDecoder());
						ch.pipeline().addLast(new BcMqttHandle());
					}

				});

	}

}
