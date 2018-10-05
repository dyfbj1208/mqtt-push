package io.mqttpush.mqttserver.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ConstantBean;
import io.mqttpush.mqttserver.beans.SendableMsg;
import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.util.CasCadeMap;
import io.mqttpush.mqttserver.util.StringCasCadeKey;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;

/**
 * 维护订阅相主题关的信息的service
 * @author acer
 *
 */
public class TopicService {

	Logger logger=Logger.getLogger(getClass());
	/**
	 * 通道订阅
	 */
	CasCadeMap<String, String> topChannels=new CasCadeMap<String, String>();
	
	ChannelUserService channelUserService;
	

	
	Map<String, ChannelGroup> mapChannelGroup;
	
	/**
	 * 发送服务
	 */
	MessagePushService messagePushService;
	
	public TopicService(){
		
		channelUserService=ServiceBeans.getInstance().getChannelUserService();
		initTopc();
		mapChannelGroup=new ConcurrentHashMap<>();
		
		ChannelGroup adminChannelGroup=new DefaultChannelGroup(new UnorderedThreadPoolEventExecutor(4));

		
		
		mapChannelGroup.putIfAbsent(ConstantBean.adminTopic, adminChannelGroup);
	}
	

	/**
	 * 处理订阅
	 * @param deviceId
	 * @param topName
	 * @param mqttQoS
	 */
	public void subscribe(String deviceId,String topName,MqttQoS mqttQoS){
	
		Channel channel=channelUserService.channel(deviceId);
	
		if(channel!=null&&channel.isActive()){
			subscribe(channel, topName, mqttQoS);
		}
	
	}
	
	

	/**
	 * 处理订阅
	 * @param deviceId
	 * @param topName
	 * @param mqttQoS
	 */
	public void subscribe(Channel channel,String topName,MqttQoS mqttQoS){
	
		String deviceId=channelUserService.deviceId(channel);
	
		if(deviceId!=null){
			
			AttributeKey<MqttQoS> attributeKey=AttributeKey.valueOf(topName);
			channel.attr(attributeKey).set(mqttQoS);
			PName pname=calPanem(topName);
			if(pname!=null) {				
				topChannels.putCasCade(pname.cname, pname.pname, deviceId);
			}
		}
		
		
		
		if(mapChannelGroup.containsKey(topName)) {
			ChannelGroup channelGroup=null;
			if((channelGroup=mapChannelGroup.get(topName))!=null) {
				channelGroup.add(channel);
			}
		}
	}
	
	
	
	/**
	 * 取消订阅
	 * @param deviceId
	 * @param topName
	 */
	public void unscribe(String deviceId,String topName){
		
		Channel channel=channelUserService.channel(deviceId);
		if(channel!=null&&channel.isActive()){
			topChannels.removeCasCade(topName, deviceId);
		}
	}
	
	public  void  initTopc(){
		
		
		StringCasCadeKey rootKey=new StringCasCadeKey("/root");
		StringCasCadeKey chatKey=new StringCasCadeKey("/chat",rootKey);
		topChannels.putCasCade(rootKey, null);
		topChannels.putCasCade(chatKey, null);

		
	}
	
	public PName calPanem(String topName){
		
		StringBuilder builder=new StringBuilder(topName);
		int  lastindex=builder.length()-1;
		
		if(builder.charAt(lastindex)=='/')
			builder.deleteCharAt(lastindex);
		
		int findex=builder.lastIndexOf("/");
		if(findex>0){
			return new PName(
					builder.substring(0,findex),
					builder.substring(findex+1));
		}
		
		
		return new PName(null,builder.toString());
	}
	/**
	 * 根据主题执行 action
	 * @param topicName
	 * @param action
	 */
	public void channelsSend(String topicName,Consumer<String> action){
			topChannels.get(topicName,action);
	}
	
	
	
	/**
	 * channnel 组发 直接发送了，不会管失败的情况
	 * @param topicName
	 * @param sendableMsg
	 */
	public void channelsForGroup(String topicName,MqttPublishMessage publishMessage) {
		
		if(mapChannelGroup.containsKey(topicName)) {
			mapChannelGroup.get(topicName).writeAndFlush(publishMessage,ChannelMatchers.all(),true);
		}
	}
	
	
	public static void main(String[] args) {
		
	     TopicService topicService=new TopicService();
	     
	     System.out.println( topicService.calPanem("/acccc/a/c/a"));
	     
	     
	}
	
	public static class PName{
		
		String pname;
		String cname;
		public PName(String pname, String cname) {
			super();
			this.pname = pname;
			this.cname = cname;
		}
		@Override
		public String toString() {
			return "PName [pname=" + pname + ", cname=" + cname + "]";
		}
		
		
	}
}
