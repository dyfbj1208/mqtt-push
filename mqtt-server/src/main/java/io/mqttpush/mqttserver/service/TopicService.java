package io.mqttpush.mqttserver.service;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * 维护订阅相主题关的信息的service
 * 
 * @author acer
 *
 */
public class TopicService {

	Logger logger = Logger.getLogger(getClass());
	/**
	 * 通道订阅
	 */
	//CasCadeMap<String, String> many2ManytopChannels = new CasCadeMap<String, String>();
	
	final  Map<String,Map<String,MqttQoS>> devSubTopics;


	ChannelUserService channelUserService;

	Map<String, ChannelGroup> mapChannelGroup;

	/**
	 * 发送服务
	 */
	MessagePushService messagePushService;

	public TopicService() {

		channelUserService = ServiceBeans.getInstance().getChannelUserService();

		mapChannelGroup = new ConcurrentHashMap<>();
		ChannelGroup adminChannelGroup = new DefaultChannelGroup(new UnorderedThreadPoolEventExecutor(4));
		mapChannelGroup.putIfAbsent(ConstantBean.adminRecivTopic, adminChannelGroup);
		devSubTopics=new ConcurrentHashMap<>();
		initTopc();
	}

	/**
	 * 处理订阅
	 * @param deviceId
	 * @param topicname
	 * @param mqttQoS
	 */
	public void subscribe(String deviceId, String topicname, MqttQoS mqttQoS) {

		Channel channel = channelUserService.channel(deviceId);

		if (channel != null && channel.isActive()) {
			subscribe(channel, topicname, mqttQoS);
		}

	}

	/**
	 * 处理订阅
	 * @param channel
	 * @param topicname
	 * @param mqttQoS
	 */
	public void subscribe(Channel channel, String topicname, MqttQoS mqttQoS) {

		if (topicname == null) {
			return;
		}

		String deviceId = channelUserService.deviceId(channel);

		if (deviceId == null) {

			logger.warn("订阅失败，怎么会出现为空的设备号?");
			return;
		}

		if (mapChannelGroup.containsKey(topicname)) {
			ChannelGroup channelGroup = null;
			if ((channelGroup = mapChannelGroup.get(topicname)) != null) {
				channelGroup.add(channel);
			}
		}
		
		

		if(!devSubTopics.containsKey(topicname)){

			logger.warn("订阅失败，订阅了无效的主题");
			return;
		}

		/**
		 * 把当前设备号和订阅的服务质量放入这个 主题下的map
		 * 方便根据主题查找设备以及服务质量
		 */
		devSubTopics.get(topicname).putIfAbsent(deviceId,mqttQoS);

		
	}

	/**
	 * 取消订阅
	 * 
	 * @param deviceId
	 * @param topName
	 */
	public void unscribe(String deviceId, String topName) {

		Channel channel = channelUserService.channel(deviceId);
		if (channel == null || (!channel.isActive())) {

			logger.warn("取消订阅失败，取消订阅的时候 必须channnel在线");
			return;
		}
	}

	/**
	 * 初始化三个订阅用的主题
	 */
	public void initTopc() {

		Map<String,MqttQoS> topicsA=new ConcurrentHashMap<>();

		Map<String,MqttQoS> topicsB=new ConcurrentHashMap<>();

		Map<String,MqttQoS> topicsC=new ConcurrentHashMap<>();

		devSubTopics.putIfAbsent("/root/topicA",topicsA);
		devSubTopics.putIfAbsent("/root/topicB",topicsB);
		devSubTopics.putIfAbsent("/root/topicC",topicsC);

	}


	/**
	 * 根据主题执行 action
	 * 
	 * @param topicName
	 * @param action
	 */
	public void channelsSend(String topicName, BiConsumer<String,MqttQoS> action) {
		if(!devSubTopics.containsKey(topicName)){
			logger.warn("发送失败，主题不存在");
			return;
		}

		devSubTopics.get(topicName).forEach(action);
	}

	/**
	 * 组发
	 * @param topicName
	 * @param publishMessage
	 */
	public void channelsForGroup(String topicName, MqttPublishMessage publishMessage) {

		if (mapChannelGroup.containsKey(topicName)) {
			mapChannelGroup.get(topicName).writeAndFlush(publishMessage, ChannelMatchers.all(), true);
		}
	}

}
