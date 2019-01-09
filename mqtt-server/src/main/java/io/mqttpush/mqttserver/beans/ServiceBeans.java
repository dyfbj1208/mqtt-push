package io.mqttpush.mqttserver.beans;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.CheckUserService;
import io.mqttpush.mqttserver.service.MessagePushService;
import io.mqttpush.mqttserver.service.TopicService;
import io.mqttpush.mqttserver.util.thread.SingelThreadPool;


/**
 * 管理用到的service bean
 * @author tianzhenjiu
 *
 */
public class ServiceBeans {


	
	Logger logger=Logger.getLogger(getClass());
	
	 ChannelUserService channelUserService;
	 CheckUserService checkUserService;
	 MessagePushService messagePushService;
	 TopicService topicService;
	 SingelThreadPool singleThreadPool;
	 

	 
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
	 * 单线程线程池
	 * @return
	 */
	public SingelThreadPool getSingleThreadPool() {
		
		if(singleThreadPool==null) {
			singleThreadPool=SingelThreadPool.getinstance();
		}
	
		return singleThreadPool;
	}

	
	 
	

	
}
