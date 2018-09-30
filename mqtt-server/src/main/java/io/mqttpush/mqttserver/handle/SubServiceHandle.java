package io.mqttpush.mqttserver.handle;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import io.mqttpush.mqttserver.service.ChannelUserService;
import io.mqttpush.mqttserver.service.TopicService;
import io.netty.channel.ChannelHandler.Sharable;
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

/**
 * 处理订阅的handle
 * 
 * @author acer
 *
 */
@Sharable
public class SubServiceHandle extends AbstractHandle {

	Logger logger = Logger.getLogger(getClass());
	
	
	ApplicationContext atx;
	
	@Autowired
	ChannelUserService channelUserService;
	
	@Autowired
	TopicService topService;
	

	
	

	public SubServiceHandle(ApplicationContext atx) {
		this.atx=atx;
		channelUserService=atx.getBean(ChannelUserService.class);
		topService=atx.getBean(TopicService.class);
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
				;//未实现
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

	
		String deviceId=channelUserService.deviceId(ctx.channel());
		int[] qoslevels = new int[list.size()];
		int i = 0;
		for (MqttTopicSubscription subscription : list) {
			qoslevels[i] = subscription.qualityOfService().value();
			topService.subscribe(deviceId, subscription.topicName(), subscription.qualityOfService());
			logger.debug(""+deviceId+"订阅了"+subscription.topicName()+subscription.qualityOfService());
		}

		if (qoslevels.length > 0) {
			MqttSubAckPayload payload = new MqttSubAckPayload(qoslevels);
			MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader
					.from(subscribeMessage.variableHeader().messageId());
			MqttSubAckMessage subAckMessage = new MqttSubAckMessage(fixedHeader, mqttMessageIdVariableHeader, payload);
			ctx.writeAndFlush(subAckMessage);
		}
	

	}

	
}
