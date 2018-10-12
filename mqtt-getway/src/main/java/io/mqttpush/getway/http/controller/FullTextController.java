package io.mqttpush.getway.http.controller;

import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;

import io.mqttpush.getway.GetWayConstantBean;
import io.mqttpush.getway.http.vo.HttpPushVo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 处理JSON的fulltext
 * 
 * @author tianzhenjiu
 *
 */
public class FullTextController extends Controller {



	final GetWayConstantBean constantBean = GetWayConstantBean.instance();

	protected FullTextController() {
		initBcChannel();
	}

	@Override
	public void service(Channel requestChannel,HttpRequest request, HttpResponse response) {

		if (!(request instanceof FullHttpRequest)) {
			response.setStatus(HttpResponseStatus.BAD_REQUEST);
			return;
		}
		
		FullHttpRequest fullHttpRequest = (FullHttpRequest) request;

		ByteBuf byteBuf = fullHttpRequest.content();

		byte[] bs = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(bs);
		
		HttpPushVo httpPushVo = JSON.parseObject(new String(bs), HttpPushVo.class);
	
		
		Channel channel=getAndSetChannelByIdentify(httpPushVo.getFromIdentify(), httpPushVo.getCallback());
		
		if(channel!=null) {
			
			
			MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE,
					false, 0);

			MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(
					constantBean.ONE2ONE_CHAT_PREFIX+httpPushVo.getToIdentify(),
					httpPushVo.hashCode());

			byteBuf=channel.alloc().buffer();
			byteBuf.writeCharSequence(httpPushVo.getContent(), Charset.forName("utf-8"));
			MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, variableHeader, byteBuf);
		
			
			
			if(channel.hasAttr(ControllBeans.loginKey)
					&&channel.attr(ControllBeans.loginKey).get()) {
				channel.writeAndFlush(mqttPublishMessage);
				requestChannel.attr(ControllBeans.requestIdentifyKey).set(httpPushVo.getFromIdentify());
			}else {
				ControllBeans.mqttPublishMessages.offer(mqttPublishMessage);
			}
			
		}

	}

	



	
	

}
