package io.mqttpush.mqttclient.service;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

/**
 * 
 * @author acer
 *
 */
public class DefaultMessageListener implements  MessageListener{

	@Override
	public void onRecivMessage(MqttPublishMessage messagepub) {
		
		ByteBuf buf=messagepub.payload();
		byte[] bs=new byte[buf.readableBytes()];
		buf.readBytes(bs);
		System.out.println(new String( bs));
	}

}
