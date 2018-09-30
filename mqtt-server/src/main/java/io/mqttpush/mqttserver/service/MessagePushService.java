package io.mqttpush.mqttserver.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import io.mqttpush.mqttserver.entity.MsgSendRunable;
import io.mqttpush.mqttserver.entity.SendMsgStatus;
import io.mqttpush.mqttserver.entity.SendMsgStatus.MsgStatus;
import io.mqttpush.mqttserver.entity.SendableMsg;
import io.netty.channel.Channel;

/**
 * message 实际发送服务
 * 
 * @author tzj
 * 
 */
public class MessagePushService {

	
	final int retimes=3;
	
	Logger logger=Logger.getLogger(getClass());


	@Autowired
	 MQManagerService mqservice;

	 
	 @Autowired
	 TopicService topicService;
	 
	 @Autowired
	 ChannelUserService channelUserService;
	 
	 /**
	  * 进行组播
	  * @param sendableMsg
	  */
	 public void sendMsg(final SendableMsg sendableMsg){
		 
		 
		 MessagePushFailHandle pushFailHandle=new MessagePushFailHandle();
		 
		 topicService.channelsExec(sendableMsg.getTopname(), (final String deviceId)->{
			 
			 	Channel channel=channelUserService.channel(deviceId);
			try {
				sendableMsg.getByteBuf().retain();
				 if(channel!=null&&
						 channel.isActive()){
					 channel.eventLoop().submit(new MsgSendRunable(deviceId,channel, sendableMsg,pushFailHandle));
				 }else{
					 pushFailHandle.handle(sendableMsg, deviceId);
				 }
			} catch (Exception e) {
				logger.debug("发送异常",e);
			}
		 });
		 
		 		 
	 }
	 
	 /**
	  * 发送失败处理
	  * @author acer
	  *
	  */
	public   class MessagePushFailHandle{
		
		int failcount=0;
		
		/**
		 * 处理发送失败
		 * @param sendableMsg
		 * @param channel
		 */
		public void handle(SendableMsg sendableMsg, String deviceId){
			
			if(failcount==0){				
				mqservice.pushMsg(sendableMsg);
			}
			failcount++;
			int i=0;
			 while(! mqservice.pushDeviceMq(deviceId, 
					 new SendMsgStatus(sendableMsg.getMessageid(),MsgStatus.SEND_FAIL))){
				 if(i>=retimes)
					 break;
			 }
		}
	}
	
     
}
