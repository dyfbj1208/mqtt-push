package io.mqttpush.mqttserver.service;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import io.mqttpush.mqttserver.util.CasCadeMap;
import io.mqttpush.mqttserver.util.StringCasCadeKey;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;

/**
 * 维护订阅相主题关的信息的service
 * @author acer
 *
 */
public class TopicService {

	/**
	 * 通道订阅
	 */
	CasCadeMap<String, String> topChannels=new CasCadeMap<String, String>();
	
	
	@Autowired
	ChannelUserService channelUserService;
	
	
	public TopicService(){
		initTopc();
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
			
			AttributeKey<MqttQoS> attributeKey=AttributeKey.valueOf(topName);
			channel.attr(attributeKey).set(mqttQoS);
			PName pname=calPanem(topName);
			topChannels.putCasCade(pname.cname, pname.pname, deviceId);
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
			topChannels.removeCasCade(topName, channelUserService.deviceId(channel));
		}
	}
	
	public  void  initTopc(){
		
		
		StringCasCadeKey rootKey=new StringCasCadeKey("/root");
		StringCasCadeKey chatKey=new StringCasCadeKey("chat",rootKey);
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
	public void channelsExec(String topicName,Consumer<String> action){
		
	
		topChannels.get(topicName,action);

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
