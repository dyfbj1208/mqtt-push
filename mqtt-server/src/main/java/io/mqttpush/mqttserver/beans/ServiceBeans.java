package io.mqttpush.mqttserver.beans;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.service.AnsyncService;
import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.CheckUserService;
import io.mqttpush.mqttserver.service.MQManagerService;
import io.mqttpush.mqttserver.service.MessagePushService;
import io.mqttpush.mqttserver.service.TopicService;

/**
 * 管理用到的service bean
 * @author tianzhenjiu
 *
 */
public class ServiceBeans {


	
	Logger logger=Logger.getLogger(getClass());
	
	
	 AnsyncService  ansyncService;
	 ChannelUserService channelUserService;
	 CheckUserService checkUserService;
	 MessagePushService messagePushService;
	 TopicService topicService;
	 MQManagerService managerService;
	 
	
	 

	 
	 static ServiceBeans serviceBeans;

	 /**
	  * 返回单利
	  * @return
	  */
	 public static ServiceBeans getInstance() {
		 
		 if(serviceBeans==null) {
			 serviceBeans=new ServiceBeans();
		 }
		 return serviceBeans;
	 }
	 
	/**
	 * @return the ansyncService
	 */
	public AnsyncService getAnsyncService() {
		
		if(ansyncService==null) {
			ansyncService=new AnsyncService();
		}
		return ansyncService;
	}
	/**
	 * @return the channelUserService
	 */
	public ChannelUserService getChannelUserService() {
		
		if(channelUserService==null) {
			channelUserService=new ChannelUserService();
		}
		return channelUserService;
	}
	/**
	 * @return the checkUserService
	 */
	public CheckUserService getCheckUserService() {
		
		if(checkUserService==null) {
			checkUserService=new CheckUserService();
		}
		return checkUserService;
	}
	/**
	 * @return the messagePushService
	 */
	public MessagePushService getMessagePushService() {
		
		if(messagePushService==null) {
			messagePushService=new MessagePushService();
		}
		return messagePushService;
	}
	/**
	 * @return the topicService
	 */
	public TopicService getTopicService() {
		
		if(topicService==null) {
			topicService=new TopicService();
		}
		return topicService;
	}
	/**
	 * @return the managerService
	 */
	public MQManagerService getManagerService() {
		
		if(managerService==null) {
			managerService=new MQManagerService();
		}
		return managerService;
	}

	
	 
	 
	

	
}
