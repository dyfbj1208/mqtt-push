package io.mqttpush.mqttserver.mqttclient.service;

import io.netty.handler.codec.mqtt.MqttPublishMessage;

/**
 * 
 * @author acer
 *
 */
public interface MessageListener {

	/**
	 * 接收到消息处
	 * @param messagepub
	 */
	public void onRecivMessage(MqttPublishMessage messagepub);
}
