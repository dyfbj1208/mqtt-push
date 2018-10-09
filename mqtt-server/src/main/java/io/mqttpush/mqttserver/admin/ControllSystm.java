package io.mqttpush.mqttserver.admin;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.MessagePushService;
import io.mqttpush.mqttserver.service.TopicService;
import io.netty.buffer.ByteBuf;

/**
 * 控制系统
 * @author tianzhenjiu
 *
 */
public class ControllSystm{

	
	Logger logger = Logger.getLogger(getClass());

	ChannelUserService channelUserService;

	MessagePushService messagePushService;
	
	TopicService topService;


	public ControllSystm() {

		ServiceBeans serviceBeans = ServiceBeans.getInstance();

		channelUserService = serviceBeans.getChannelUserService();

		messagePushService = serviceBeans.getMessagePushService();
		
		topService = serviceBeans.getTopicService();

	}
	
	public void handleMessage(ByteBuf byteBuf) {
		
	}
	
}
