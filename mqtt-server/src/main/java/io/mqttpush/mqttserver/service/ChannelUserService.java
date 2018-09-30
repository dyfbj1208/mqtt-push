package io.mqttpush.mqttserver.service;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 管理者登录信息的设备号以及channel
 * 
 * @author tzj
 *
 */
public class ChannelUserService {

	Logger logger = Logger.getLogger(getClass());

	/**
	 * 用于根据登录的客户端标识找channel
	 */
	ConcurrentHashMap<String, Channel> str2channel = new ConcurrentHashMap<>();

	/**
	 * 判断是否登录 处理重复登录
	 * 
	 * @param ident
	 */
	public void processReLogin(String ident) {

		if (str2channel.containsKey(ident)) {// 重复登录

			Channel channelold = str2channel.get(ident);
			str2channel.remove(ident);
			channelold.close();
			logger.debug("重复登录关闭以前的channel" + channelold);
		}
	}

	/**
	 * 退出
	 * 
	 * @param handlerContext
	 */
	public void loginout(Channel channel) {

		String iden = deviceId(channel);

			if (iden != null && str2channel.containsKey(iden)){
				if(str2channel.get(iden)==channel){
					str2channel.remove(iden);
				}
			}
			
			if(channel.isActive())
				channel.close();
		
			logger.debug(iden + "退出,在线人数\t" + str2channel.size());
			

	}

	/**
	 * 成功登录
	 * 
	 * @param ident
	 * @param channel
	 */
	public void loginSuccess(String ident, Channel channel) {

		final Channel channel2 =  str2channel.put(ident, channel);;
		if (channel2 != null) {
			MqttFixedHeader fixedHeader=new MqttFixedHeader(
					MqttMessageType.DISCONNECT, 
					false,
					MqttQoS.AT_MOST_ONCE, false, 0);
			MqttMessage dismessage=new MqttMessage(fixedHeader);
			ChannelFuture channelFuture=channel2.writeAndFlush(dismessage);
			channelFuture.addListener(new GenericFutureListener<Future<Void>>() {
				@Override
				public void operationComplete(Future<Void> future) throws Exception {
					channel2.close();
				}
				
			});
			
			
		}

		AttributeKey<String> deviceKey = AttributeKey.valueOf("deviceId");
		AttributeKey<Boolean> loginKey = AttributeKey.valueOf("login");
		channel.attr(deviceKey).set(ident);
		channel.attr(loginKey).set(true);
		logger.debug(ident + "登陆成功");

	}

	/**
	 * 是否登录
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean isLogin(String deviceId) {
		return str2channel.containsKey(deviceId);
	}

	/**
	 * 是否登录
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean isLogin(Channel channel) {

		AttributeKey<Boolean> loginKey = AttributeKey.valueOf("login");
		return channel != null && channel.hasAttr(loginKey);
	}

	/**
	 * 根据信道返回设备id
	 * 
	 * @param channel
	 * @return
	 */
	public String deviceId(Channel channel) {

		if (isLogin(channel)) {
			AttributeKey<String> deviceKey = AttributeKey.valueOf("deviceId");
			return channel.attr(deviceKey).get();
		}

		return null;
	}

	public Channel channel(String deviceId) {
		return str2channel.get(deviceId);
	}

}
