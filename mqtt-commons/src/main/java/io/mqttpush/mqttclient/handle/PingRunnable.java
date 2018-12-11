package io.mqttpush.mqttclient.handle;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import io.mqttpush.mqttclient.conn.Connetor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 发送心跳
 * 
 * @author tianzhenjiu
 *
 */
public class PingRunnable implements Runnable {

	Logger logger = Logger.getLogger(getClass());

	final Channel channel;

	final AtomicBoolean isValidate;

	final Connetor connetor;

	final ScheduledExecutorService executorService;
	
	final Integer pingTime;

	public PingRunnable(Channel channel, AtomicBoolean isValidate, Connetor connetor,
			Integer pingTime,
			ScheduledExecutorService executorService) {
		super();
		this.channel = channel;
		this.isValidate = isValidate;
		this.connetor = connetor;
		this.pingTime=pingTime;
		this.executorService = executorService;
	}

	@Override
	public void run() {

		/**
		 * 只要链路不可用，或者没有收到心跳回复就重新连接
		 */
		if (!channel.isActive() || (!isValidate.get())) {
			connetor.reconnection(channel);
			logger.warn("链路不可用,重新连接");
			return;
		}

		channel.writeAndFlush(
				new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0)))
				.addListener((ChannelFuture future) -> {

					if (logger.isDebugEnabled()) {
						logger.debug("发送心跳");
					}

					/**
					 * 只要心跳发出去了就设置了 没收到心跳
					 */
					if (isValidate.compareAndSet(true, false)) {
						executorService.schedule(PingRunnable.this, pingTime, TimeUnit.SECONDS);
					}

				});

	}

}
