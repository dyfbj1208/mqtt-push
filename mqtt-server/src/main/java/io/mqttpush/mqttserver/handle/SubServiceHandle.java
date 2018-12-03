package io.mqttpush.mqttserver.handle;

import java.util.List;

import org.apache.log4j.Logger;

import io.mqttpush.mqttserver.beans.ServiceBeans;
import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.TopicService;
import io.mqttpush.mqttserver.util.thread.MyHashRunnable;
import io.mqttpush.mqttserver.util.thread.SignelThreadPoll;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;

/**
 * 处理订阅的handle
 * 
 * @author acer
 *
 */
public class SubServiceHandle extends AbstractHandle {

	Logger logger = Logger.getLogger(getClass());

	ChannelUserService channelUserService;

	TopicService topService;

	SignelThreadPoll signelThreadPoll;
	
	public SubServiceHandle() {
		
		ServiceBeans serviceBeans=ServiceBeans.getInstance();
		
		channelUserService = serviceBeans.getChannelUserService();
		topService = serviceBeans.getTopicService();
		signelThreadPoll=serviceBeans.getSignelThreadPoll();
	}

	@Override
	public void onMessage(ChannelHandlerContext ctx, MqttMessage msg) {
		
		if (msg instanceof MqttMessage) {

			MqttMessage message = (MqttMessage) msg;
			MqttMessageType messageType = message.fixedHeader().messageType();
			switch (messageType) {
			case SUBSCRIBE:
				sub(ctx, (MqttSubscribeMessage) message);
				break;
			case UNSUBSCRIBE:
				 unSub(ctx, (MqttUnsubscribeMessage) msg);
			default:
				ctx.fireChannelRead(msg);
				break;
			}

		}

		else
			ctx.close();
	}

	/**
	 * 订阅操作 向服务器订阅所有感兴趣的主题 并且按照客户端标识找到未读消息接受这些消息
	 * 
	 * @param ctx
	 * @param subscribeMessage
	 */
	private void sub(final ChannelHandlerContext ctx, MqttSubscribeMessage subscribeMessage) {

		MqttQoS mqttQoS = subscribeMessage.fixedHeader().qosLevel();

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, mqttQoS, false, 0);

		List<MqttTopicSubscription> list = subscribeMessage.payload().topicSubscriptions();

	
	
		int[] qoslevels = new int[list.size()];
		int i = 0;
		for (MqttTopicSubscription subscription : list) {
			qoslevels[i] = subscription.qualityOfService().value();
			String topicName=subscription.topicName();
			topService.subscribe(ctx.channel(), topicName, subscription.qualityOfService());
		
			
			if(logger.isDebugEnabled()) {
				
				String deviceId=channelUserService.deviceId(ctx.channel());
				logger.debug(""+deviceId+"订阅了"+topicName+subscription.qualityOfService());
			}
			
			
		}

		if (qoslevels.length > 0) {
			MqttSubAckPayload payload = new MqttSubAckPayload(qoslevels);
			MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader
					.from(subscribeMessage.variableHeader().messageId());
			MqttSubAckMessage subAckMessage = new MqttSubAckMessage(fixedHeader, mqttMessageIdVariableHeader, payload);
			ctx.writeAndFlush(subAckMessage);
		}
	

	}
	
	/**
	 *取消订阅
	 * @param ctx
	 * @param unsubscribeMessage
	 */
	public void unSub(final ChannelHandlerContext ctx, MqttUnsubscribeMessage unsubscribeMessage) {
		
		MqttQoS mqttQoS = unsubscribeMessage.fixedHeader().qosLevel();

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, mqttQoS, false, 0);

		List<String> list = unsubscribeMessage.payload().topics();
		
		final String  deviceId=channelUserService.deviceId(ctx.channel());
		
		if(list!=null&&!list.isEmpty()) {
			list.forEach((topic)->{
			
				Runnable unScrRun=()->{
					topService.unscribe(deviceId, topic);
				};
					
				signelThreadPoll.execute(new MyHashRunnable(deviceId, unScrRun, 0));
				
			});
		}

		MqttUnsubAckMessage unsubAckMessage=new MqttUnsubAckMessage(fixedHeader, MqttMessageIdVariableHeader.from(unsubscribeMessage.variableHeader().messageId()));
		
		ctx.write(unsubAckMessage);
	}

	
}
